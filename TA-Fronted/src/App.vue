<script setup>
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { UserControllerService } from '@/generated/services/UserControllerService'
import { AiCompanyControllerService } from './generated';
import { ConsultantControllerService } from './generated';
import { useNavStore } from './stores/nav';
import useCosStore from './stores/cos';
import 'vant/es/toast/style'
import { showToast } from 'vant';
import useUserInfoStore from './stores/userinfo';
import { DriftControllerService } from './generated';
import useBottleStore from './stores/bottle';

const router = useRouter();
const NavStore = useNavStore();
const UserInfoStore = useUserInfoStore();
const CosStroe = useCosStore()
const BottleStore = useBottleStore()

// 路由拦截，需要先进行登录
router.beforeEach(async (to, from, next) => {

  // 自动登录逻辑
  if (to.name === 'login') {
    try {
      // 获取登录信息
      const res = await UserControllerService.getLoginUserUsingGet()
      if (res.code === 0) {
        // 更新用户信息
        UserInfoStore.userinfo.userAvatar = res.data.userAvatar;
        UserInfoStore.userinfo.userName = res.data.userName;
        UserInfoStore.userinfo.id = res.data.id;
        next('/seeuindex')
      } else {
        next()
      }
    } catch (err) {
      console.log(err)
      showToast("自动登录失败，服务器或网络问题")
      next()
    }
  }
  else {
    next()
  }

  // 底部导航对应逻辑
  if (/\/seeuindex.*/.test(to.path)) {
    NavStore.active = 0;
  } else if (/\/me.*/.test(to.path)) {
    NavStore.active = 3;
  } else if (/\/dbottles/.test(to.path)) {
    NavStore.active = 2;
  } else {
    NavStore.active = 1;
  }


  // 剩余次数逻辑
  if (to.path == '/seeuindex') {
    try {
      let res = await UserControllerService.getUseTimesUsingGet(UserInfoStore.userinfo.id)
      UserInfoStore.userinfo.restTime = res.data;
    } catch (e) {
      showToast('请求剩余次数失败'+e)
    }
  }

  // AI小伴开始逻辑
  if (to.name == 'friend') {
    // 未开启对话，则需要开启一轮新对话
    if (UserInfoStore.userinfo.chatCompanyID == "") {
      let res;
      try {
        res = await AiCompanyControllerService.startChatUsingPost()
        if (res.code == 0) {
          UserInfoStore.userinfo.chatCompanyID = res.data;
        } else {
          throw (res.message)
        }
      } catch (e) {
        showToast('发起对话失败' + e)
      }
    }
  }
  // AI心理咨询开始逻辑
  if (to.name == 'consultation') {
    // 未开启对话，则需要开启一轮新对话
    if (UserInfoStore.userinfo.chatConsultationID == "") {
      let res;
      try {
        res = await ConsultantControllerService.startChatUsingPost1()
        if (res.code == 0) {
          UserInfoStore.userinfo.chatConsultationID = res.data;
        } else {
          throw (res.message)
        }
      } catch (e) {
        showToast('发起对话失败' + e)
      }
    }
  }

  // 实时头像url更新
  if (to.path == '/me') {
    if (UserInfoStore.userinfo.userAvatar == "") {
      UserInfoStore.userinfo.userAvatarUrl = "@/assets/logo.png"
    }
    // 如果没有上传过avatar，则使用默认头像 
    else {
      UserInfoStore.userinfo.userAvatarUrl = CosStroe.getUrl(UserInfoStore.userinfo.userAvatar)
    }
  }

  // 去往sendlist
  if (to.name == 'sendlist') {
    let res;
    try {
      res = await DriftControllerService.listMyBottleVoByPageUsingPost({
        userId: UserInfoStore.userinfo.id,
        isPick: 1
      })
      // 请求成功
      if (res.code == 0) {
        BottleStore.bottleListSendCatched = res.data.records
      } else {
        throw (res.message)
      }
    } catch (e) {
      showToast("请求已捞取消息列表失败" + e)
    }
  }

  if (to.name == 'sendlist') {
    let res;
    try {
      res = await DriftControllerService.listMyBottleVoByPageUsingPost({
        userId: UserInfoStore.userinfo.id,
        isPick: 0
      })
      // 请求成功
      if (res.code == 0) {
        BottleStore.bottleListSendUncatched = res.data.records
      } else {
        throw (res.message)
      }
    } catch (e) {
      showToast("请求未捞取消息列表失败" + e)
    }
  }

  if (to.name == 'recvlist') {
    let res;
    try {
      console.log(UserInfoStore.userinfo.id);
      res = await DriftControllerService.listMyBottleVoByPageUsingPost({
        pickUserId: UserInfoStore.userinfo.id
      })
      // 请求成功
      if (res.code == 0) {
        console.log(res.data.records);
        BottleStore.bottleListRecv = res.data.records
      } else {
        throw (res.message)
      }
    } catch (e) {
      showToast("请求消息列表失败" + e)
    }
  }

})

</script>

<template>
  <RouterView />
</template>

<style scoped></style>