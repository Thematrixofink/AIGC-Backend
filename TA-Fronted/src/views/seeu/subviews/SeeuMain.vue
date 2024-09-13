<!-- 再见一面对话界面 -->

<template>
    <!-- 头部 -->
    <van-nav-bar title="爷爷" class="nav-bar" />
    <div class="seeu-main">
        <!-- 视频播放 -->
        <div class="video">
            <!-- <video src=""></video> -->
        </div>

        <!-- 自己的画面 -->
        <div class="myvideo"></div>

        <!-- 对话界面 -->
        <div class="text">
            <chat-box text="你好你好你好你好你好你好你好你好你好你你好你好你好你好你好你好你好你好你好你你好你好你好你好你好你好你好你好你好你你好你好你好你好你好你好你好你好你好你你好你好你好你好你好你好你好你好你好你你好你好你好你好你好你好你好你好你好你你好你好你好你好你好你好你好你好你好你你好你好你好你好你好你好你好你好你好你你好你好你好你好你好你好你好你好你好你你好你好你好你好你好你好你好你好你好你你好你好你好你好你好你好你好你好你好你你好你好你好你好你好你好你好你好你好你你好你好你好你好你好你好你好你好你好你你好你好你好你好你好你好你好你好你好你你好你好你好你好你好你好你好你好你好你你好你好你好你好你好你好你好你好你好你你好你好你好你好你好你好你好你好你好你你好你好你好你好你好你好你好你好你好你你好你好你好你好你好你好你好你好你好你你好你好你好你好你好你好你好你好你好你" type="left" name="爷爷"/>
            <chat-box text="我很好" type="right" name="我"/>
            <chat-box text="你好" type="left" name="爷爷"/>
            <chat-box text="我很好" type="right" name="我"/>
            <chat-box text="你好"/>
            <chat-box text="我很好"/>
        </div>

        <!-- 倒计时 -->
        <div class="counter">
            倒计时:&nbsp;<van-count-down :time="time" />
        </div>

        <!-- 说话按钮 -->
        <div class="speak-btn">
            <van-button type="primary">说话按钮</van-button>
        </div>

        <!-- 挂断 -->
        <div class="cut-off" @click="cutOff">
            挂断通话
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import ChatBox from '@/components/ChatBox.vue';

const time = ref(1000);
const router = useRouter()

// 挂断后处理
const cutOff = () => {
    router.replace('/seeuindex')
}


</script>

<style scoped lang="less">
.nav-bar {
    background-color: #F2F5FF;
    --van-nav-bar-icon-color: #333333;
}

.seeu-main {
    text-align: center;
    width: 100vw;
    margin-top: 4.5vw;
    overflow: hidden;

    .video {
        width: 92vw;
        height: 83vw;
        margin: 0 auto;
        background-image: url("https://th.bing.com/th/id/OIP.jr0DsER8j9Tpm0Yqs5mGSwHaLG?rs=1&pid=ImgDetMain");
        background-size: cover;
        border-radius: 16px;
    }

    .text {
        margin: 3.3vw auto;
        box-sizing: border-box;
        width: 92vw;
        height: 42vw;
        border-radius: 20px;
        background-color: #F3F3F4;
        padding: 2vw 1px;
        overflow-y: auto;
        overflow-x: hidden;
    }

    .counter {
        margin: 0 auto;
        display: flex;
        justify-content: center;
        font-size: 10px;
        --van-count-down-text-color: #5473E8;
        --van-count-down-font-size: 17px;
    }

    .cut-off {
        margin-top: 10px;
        font-size: 10px;
        text-decoration: underline;
        color: #CC2525;
    }
}
</style>