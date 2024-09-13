package com.ink.backend.controller;


import cn.hutool.core.io.unit.DataUnit;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ink.backend.annotation.AuthCheck;
import com.ink.backend.common.BaseResponse;
import com.ink.backend.common.DeleteRequest;
import com.ink.backend.common.ErrorCode;
import com.ink.backend.common.ResultUtils;
import com.ink.backend.constant.LimitBusinessConstant;
import com.ink.backend.constant.UserConstant;
import com.ink.backend.exception.BusinessException;
import com.ink.backend.exception.ThrowUtils;
import com.ink.backend.manager.CosManager;
import com.ink.backend.model.dto.bottle.BottleAddRequest;
import com.ink.backend.model.dto.bottle.BottleCommentRequest;
import com.ink.backend.model.dto.bottle.BottleQueryRequest;
import com.ink.backend.model.dto.user.UserAddRequest;
import com.ink.backend.model.entity.Bottle;
import com.ink.backend.model.entity.Bottlecomment;
import com.ink.backend.model.entity.User;
import com.ink.backend.model.vo.BottleCommentVO;
import com.ink.backend.model.vo.BottleVO;
import com.ink.backend.service.BottleService;
import com.ink.backend.service.BottlecommentService;
import com.ink.backend.service.UserService;
import com.ink.backend.utils.NetUtils;
import com.ink.backend.utils.TimeUtils;
import com.qcloud.cos.model.ciModel.auditing.AuditingJobsDetail;
import com.qcloud.cos.model.ciModel.auditing.TextAuditingResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 漂流瓶
 */
@RestController
@RequestMapping("/drift")
@Slf4j
public class DriftController {

    @Resource
    private BottleService bottleService;
    @Resource
    private UserService userService;
    @Resource
    private BottlecommentService bottlecommentService;

    @Resource
    private RedisTemplate<String,Integer> redisTemplate;
    @Resource
    private CosManager cosManager;

    // region 增删改查

    /**
     * 创建漂流瓶
     *
     * @param bottleAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addBottle(@RequestBody BottleAddRequest bottleAddRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        //检查用户是否还有扔瓶子的次数
        boolean havaTimes = checkDailyUseTimes(userId, LimitBusinessConstant.DRIFT_ADD_PREFIX, LimitBusinessConstant.DRIFT_ADD_TIMES);
        if(!havaTimes) throw new BusinessException(ErrorCode.FORBIDDEN_ERROR,"今天扔了太多了，明天再来吧");

        if (bottleAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String content = bottleAddRequest.getContent();
        //1.校验漂流瓶内容是否合法
        if(StringUtils.isBlank(content)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"漂流瓶内容为空!");
        }
        //漂流瓶内容长度是否过长
        if(content.length() > 200){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"漂流瓶内容过长");
        }
        boolean b = textAuditing(content);
        if(!b){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请文明语言!");
        }
        Bottle bottle = new Bottle();
        bottle.setContent(content);
        bottle.setUserId(loginUser.getId());
        boolean result = bottleService.save(bottle);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newPostId = bottle.getId();
        //用户的使用次数加1
        Integer preUseTimes = redisTemplate.opsForValue().get(LimitBusinessConstant.DRIFT_ADD_PREFIX + userId);
        preUseTimes++;
        Integer expireTime = getExpireTime();
        redisTemplate.opsForValue().set(LimitBusinessConstant.DRIFT_ADD_PREFIX + userId,preUseTimes,expireTime,TimeUnit.SECONDS);
        return ResultUtils.success(newPostId);
    }

    /**
     * 删除漂流瓶,用户或管理员可操作
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteBottle(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Bottle bottle = bottleService.getById(id);
        ThrowUtils.throwIf(bottle == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!bottle.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = bottleService.removeById(id);
        // 删除此瓶子的评论
        LambdaQueryWrapper<Bottlecomment> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(Bottlecomment::getBottleId,id);
        boolean remove = bottlecommentService.remove(deleteWrapper);
        return ResultUtils.success(b);
    }


    /**
     * 根据 id 获取漂流瓶,仅限用户
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<BottleVO> getBottleVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Bottle bottle = bottleService.getById(id);
        if (bottle == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        BottleVO bottleVO = BottleVO.objToVo(bottle);
        //获取瓶子的评论
        LambdaQueryWrapper<Bottlecomment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Bottlecomment::getBottleId,id);
        //查询到所有与此瓶子相关的评论
        List<Bottlecomment> allComments = bottlecommentService.list(wrapper);
        if(allComments == null || allComments.size() == 0){
            bottleVO.setComments(null);
            return ResultUtils.success(bottleVO);
        }
        //对评论进行处理
        //1.将评论按照parentId的大小进行排序，parentId小的评论在上面
        List<Bottlecomment> sortComments = allComments.stream().sorted((o1, o2) -> (int) (o1.getParentId() - o2.getParentId())).collect(Collectors.toList());
        //2.将comment转换为commentVO
        Bottlecomment parentComment = sortComments.get(0);
        Long userId = parentComment.getUserId();
        Long replyUserId = parentComment.getReplyUserId();
        //获取回复和被回复用户的信息
        User user = userService.getById(userId);
        String userName = user.getUserName();
        String userAvatar = user.getUserAvatar();
        User replyUser = userService.getById(replyUserId);
        String replyUserName = replyUser.getUserName();
        String replyUserAvatar = replyUser.getUserAvatar();
        List<BottleCommentVO> collectComments = sortComments.stream().map(bottlecomment -> {
            BottleCommentVO bottleCommentVO = new BottleCommentVO();
            BeanUtils.copyProperties(bottlecomment, bottleCommentVO);
            Long tempUserId = bottlecomment.getUserId();
            Long tempReplyUserId = bottlecomment.getReplyUserId();
            if (tempUserId.equals(userId)) {
                bottleCommentVO.setUserName(userName);
                bottleCommentVO.setUserAvatar(userAvatar);
            }
            if (tempUserId.equals(replyUserId)) {
                bottleCommentVO.setUserName(replyUserName);
                bottleCommentVO.setUserAvatar(replyUserAvatar);
            }
            if (tempReplyUserId.equals(userId)) {
                bottleCommentVO.setReplyUserName(userName);
                bottleCommentVO.setReplyUserAvatar(userAvatar);
            }
            if (tempReplyUserId.equals(replyUserId)) {
                bottleCommentVO.setReplyUserName(replyUserName);
                bottleCommentVO.setReplyUserAvatar(replyUserAvatar);
            }
            return bottleCommentVO;
        }).collect(Collectors.toList());
        bottleVO.setComments(collectComments);
        return ResultUtils.success(bottleVO);
    }


    /**
     * 分页获取当前用户创建的漂流瓶列表
     *
     * @param bottleQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<BottleVO>> listMyBottleVOByPage(@RequestBody BottleQueryRequest bottleQueryRequest,
                                                           HttpServletRequest request) {
        if (bottleQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long current = bottleQueryRequest.getCurrent();
        long size = bottleQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Bottle> bottlePage = bottleService.page(new Page<>(current, size),
                bottleService.getQueryWrapper(bottleQueryRequest));
        return ResultUtils.success(bottleService.getBottleVOPage(bottlePage, request));
    }


    /**
     * 打捞一个漂流瓶
     * @param request
     * @return
     */
    @GetMapping("/pick")
    public BaseResponse<BottleVO> pickBottle(HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        Long pickUserId = loginUser.getId();
        boolean b = checkDailyUseTimes(pickUserId, LimitBusinessConstant.DRIFT_PICK_PREFIX, LimitBusinessConstant.DRIFT_PICK_TIMES);
        if(!b) throw new BusinessException(ErrorCode.FORBIDDEN_ERROR,"今天捞了太多了，明天再来吧");

        BottleQueryRequest bottleQueryRequest = new BottleQueryRequest();
        bottleQueryRequest.setNotId(pickUserId);
        bottleQueryRequest.setIsPick(0);
        QueryWrapper<Bottle> queryWrapper = bottleService.getQueryWrapper(bottleQueryRequest);
        Bottle bottle = bottleService.getOne(queryWrapper,false);
        if(ObjectUtils.isEmpty(bottle)){
            return ResultUtils.error(ErrorCode.NOT_FOUND_ERROR,"瓶子已经被捞完了");
        }
        Long userId = bottle.getUserId();
        User user = userService.getById(userId);
        BottleVO bottleVO = BottleVO.objToVo(bottle);
        bottleVO.setUserName(user.getUserName());
        bottleVO.setUserAvatar(user.getUserAvatar());
        bottleVO.setPickUserName(loginUser.getUserName());
        bottleVO.setPickUserAvatar(loginUser.getUserAvatar());
        //修改原来的瓶子为已被捞起状态
        bottle.setPickUserId(pickUserId);
        bottle.setIsPick(1);
        bottleService.updateById(bottle);
        //用户的使用次数加1
        Integer preUseTimes = redisTemplate.opsForValue().get(LimitBusinessConstant.DRIFT_PICK_PREFIX + pickUserId);
        preUseTimes++;
        Integer expireTime = getExpireTime();
        redisTemplate.opsForValue().set(LimitBusinessConstant.DRIFT_PICK_PREFIX + pickUserId,preUseTimes,expireTime,TimeUnit.SECONDS);
        return ResultUtils.success(bottleVO);
    }

    /**
     * 对漂流瓶进行评论
     * @param bottleCommentRequest
     * @param request
     * @return
     */
    @PostMapping("/comment")
    public BaseResponse<BottleCommentVO> commentBottle(@RequestBody BottleCommentRequest bottleCommentRequest, HttpServletRequest request){
        //1.校验参数
        if (bottleCommentRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long bottleId = bottleCommentRequest.getBottleId();
        Long parentId = bottleCommentRequest.getParentId();
        Long replyUserId = bottleCommentRequest.getReplyUserId();
        String detail = bottleCommentRequest.getDetail();
        Long loginUserId = userService.getLoginUser(request).getId();
        User user = userService.getById(loginUserId);
        if(StringUtils.isBlank(detail)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"评论内容为空!");
        }
        if(detail.length() > 200){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"评论太长了");
        }
        boolean b = textAuditing(detail);
        if(!b){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请文明语言!");
        }
        Bottle bottle = bottleService.getById(bottleId);
        if (bottle == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
//        if(parentId != 0) {
//            Bottlecomment parentComment = bottlecommentService.getById(parentId);
//            if (parentComment == null) {
//                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
//            }
//        }
        User replyUser = userService.getById(replyUserId);
        if(replyUser == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //2.添加评论
        Bottlecomment bottlecomment = new Bottlecomment();
        bottlecomment.setBottleId(bottleId);
        //bottlecomment.setParentId(parentId);
        bottlecomment.setUserId(loginUserId);
        bottlecomment.setReplyUserId(replyUserId);
        bottlecomment.setDetail(detail);
        bottlecommentService.save(bottlecomment);
        Bottlecomment byId = bottlecommentService.getById(bottlecomment.getId());
        //3.获取评论返回类
        BottleCommentVO bottleCommentVO = new BottleCommentVO();
        BeanUtils.copyProperties(bottlecomment,bottleCommentVO);
        bottleCommentVO.setUserName(user.getUserName());
        bottleCommentVO.setUserAvatar(user.getUserAvatar());
        bottleCommentVO.setReplyUserName(replyUser.getUserName());
        bottleCommentVO.setReplyUserAvatar(replyUser.getUserAvatar());
        bottleCommentVO.setCreateTime(byId.getCreateTime());
        return ResultUtils.success(bottleCommentVO);
    }

    // endregion


    /**
     * 识别文字是否合法
     * @param content
     * @return
     */
    private boolean textAuditing(String content){
        AuditingJobsDetail auditingDetail = cosManager.textAuditing(content);
        String textResult = auditingDetail.getResult();
        if(textResult.equals("1")){
            return false;
        }
        return true;
    }


    /**
     * 检查是否还有每日使用次数
     * @param userId
     * @param businessKey
     * @param dailyMaxTimes
     * @return
     */
    private boolean checkDailyUseTimes(Long userId,String businessKey,Integer dailyMaxTimes){
        String key = businessKey + userId;
        Integer useTimes = redisTemplate.opsForValue().get(key);
        //说明今天还没有使用过,为其设置value和Key
        if(useTimes == null){
            Integer expireTime = getExpireTime();
            redisTemplate.opsForValue().set(key,0,expireTime, TimeUnit.SECONDS);
            return true;
        }
        if(useTimes >= dailyMaxTimes){
            System.out.println("今天次数已经没有了");
            return false;
        }
        return true;
    }

    /**
     * 获取过期时间
     * @return
     */
    private Integer getExpireTime(){
        LocalDateTime currentTime = LocalDateTime.now();
        Integer remainSecondsOneDay = TimeUtils.getRemainSecondsOneDay(currentTime);
        return remainSecondsOneDay;
    }

}
