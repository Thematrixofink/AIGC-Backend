<!-- 再见一面模块上传文件页面 -->
<!-- 提供图片、音频/视频、亲人描述上传接口 -->
<!-- props: null -->

<template>
  <!-- 头部 -->
  <Header title="文件上传" />

  <!-- 上传主体部分 -->
  <div class="seeuupload-content">

    <!-- 上传图片 -->
    <div class="text-img">&nbsp;&nbsp;上传照片</div>
    <div class="img-uploader">
      <FileUploader type="img" :addUrl="addUrl" />
      <!-- 示例图片 -->
      <div class="img-demo">
        <div class="over-text">
          示&nbsp;例&nbsp;图&nbsp;片
        </div>
      </div>
    </div>

    <!-- 上传音频 -->
    <div class="text-img" style="margin-top: 5px">&nbsp;&nbsp;上传声音</div>
    <div class="voice-uploader">
      <div class="audio-uploader">
        <FileUploader type="unable" :addUrl="addUrl" v-if="audio_canupload" />
        <FileUploader type="audio" :addUrl="addUrl" v-else />
      </div>

      <!-- 上传视频 -->
      <div class="video-uploader">
        <FileUploader type="unable" :addUrl="addUrl" v-if="video_canupload" />
        <FileUploader type="video" :addUrl="addUrl" v-else />
      </div>
    </div>


    <!-- 上传描述 -->
    <div class="description-uploader">
      <van-cell-group inset>
        <van-field v-model="name" rows="2" autosize label="TA的称呼" type="input" placeholder="TA是你的谁?" show-word-limit />
        <div style="text-align:left; font-size:14px; padding: 0 16px; margin-top:5px;">描述</div>
        <div class="description-box">
          <van-field v-model="description" rows="2" label="" type="textarea" maxlength="500" placeholder="请输入描述"
            :autosize="{ maxHeight: 80, minHeight: 80 }" />
        </div>
      </van-cell-group>

    </div>

    <!-- 文件上传说明 -->
    <div class="notion">
      文件上传说明
    </div>

    <!-- 提交按钮 -->
    <van-button type="primary" @click="submitFiles" class="commit-button" color="#5473E8">提交</van-button>

  </div>
</template>

<script setup>
import Header from '@/components/Header.vue';
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import FileUploader from '../components/FileUploader.vue';
import 'vant/es/toast/style'
import { showToast } from 'vant';
import { AiPersonInfoControllerService } from '@/generated';
import useUserInfoStore from '@/stores/userinfo';

const router = useRouter();
const description = ref("")
const name = ref("")
const UserInfoStore = useUserInfoStore()

// 上传的信息对象，当fileuploader中有信息上传时，会在其中添加对应url
const urls = ref({});

const submitFiles = async () => {
  // 校验音频和图片文件是否都有上传
  if (('img' in urls.value) && ('audio' in urls.value || 'video' in urls.value) && name.value != "" && description.value != "") {
    // 通过后进入下一步
    try {
      // 建立数字人信息
      let res = await AiPersonInfoControllerService.preGeneratorUsingPost(
        {
          aiName: name.value,
          aiPicture: urls.value.img, //如果是只生成音频，那么就不需要上传，如果生成视频，比如生产
          aiProfile: description.value,
          aiVoice: ('audio' in urls.value) ? urls.value.audio : urls.value.video //必须上传的
        }
      )
      if (res.code == 0) {
        // 获取messeageid
        UserInfoStore.userinfo.messageid = res.data
        router.replace('seeuready')
        return;
      }else {
        showToast(res.message)
      }
    } catch (err) {
      showToast('信息上传失败，网络或服务器问题')
    }
  } else {
    showToast('请上传完整信息')
  }
}

const addUrl = (key, value) => {
  urls.value[key] = value;
}

// 组件显示计算属性
const audio_canupload = computed(() => {
  return 'video' in urls.value;
})
const video_canupload = computed(() => {
  return 'audio' in urls.value;
})


</script>

<style scoped lang="less">
.seeuupload-content {
  text-align: center;

  .text-img {
    margin-top: 20px;
    text-align: left;
    font-family: "Source Han Sans CN";
    font-weight: 400;
    font-size: 4vw;
  }

  .img-uploader {
    margin-top: 8px;
    display: flex;
    justify-content: space-around;
    align-items: center;

    .img-demo {
      background-image: url("https://th.bing.com/th/id/OIP.jr0DsER8j9Tpm0Yqs5mGSwHaLG?rs=1&pid=ImgDetMain");
      background-repeat: no-repeat;
      background-size: cover;
      width: 35vw;
      height: 39vw;
      border-radius: 15px;
      display: flex;
      flex-direction: column-reverse;

      .over-text {
        text-align: center;
        width: 35vw;
        height: 7vw;
        font-family: 'Source Han Sans CN';
        background-color: rgba(0, 0, 0, 0.3);
        color: white;
        border-radius: 0 0 15px 15px;
        line-height: 7vw;
        font-size: 3.7vw;
      }
    }
  }

  .voice-uploader {
    display: flex;
    justify-content: center;
    margin-top: 5vw;

    .audio-uploader {
      text-align: center;
      margin: 0 2.5vw 0 3.5vw;
    }

    .video-uploader {
      text-align: center;
      margin: 0 2.5vw 0;
    }
  }

  .description-uploader {
    .description-box {
      margin: 5px auto;
      width: 93vw;
      height: 28vw;
      background-color: #F3F3F4;
      border-radius: 8px;
    }
  }

  .notion {
    margin: 10px auto;
    padding: 10px;
    text-align: left;
    width: 90vw;

  }

  .commit-button {
    margin: 0 0 8vh 0;
    --van-button-default-height: 8vw;
    --van-button-radius: 1.8vw;
    padding: 0 10vw;
  }
}


:deep(.van-field__label) {
  width: 60px;
}

:deep(.van-cell-group--inset) {
  margin: 0;
}

:deep(.van-cell) {
  background-color: transparent;
}
</style>