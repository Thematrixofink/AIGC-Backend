<template>
    <van-nav-bar title="再见一面" style="background-color: #F2F5FF; font-family: 'Source Han Sans CN';" />
    <div class="seeuindex-content">

        <!-- 使用方法及说明 -->
        <div class="claim">
            使用方法及使用说明
        </div>

        <!-- 剩余次数 -->
        <div class="rest-times">
            剩余次数：<span>{{ UserInfoStore.userinfo.restTime }}次</span>
        </div>

        <!-- 开始按钮 -->
        <div class="confirm">
            <van-button type="default" @click="enterSeeu">开启再见一面</van-button>
        </div>

    </div>
    <NavFooter />
</template>

<script setup>
import NavFooter from '@/components/NavFooter.vue';
import { useRouter } from 'vue-router';
import useUserInfoStore from '@/stores/userinfo';
import { ref, reactive } from 'vue';

const router = useRouter()
const UserInfoStore = useUserInfoStore()

const enterSeeu = () => {
    router.push('/seeuupload')
}

</script>


<style scoped lang="less">
.seeuindex-content {
    text-align: center;

    .claim {
        width: 82vw;
        height: 60vh;
        border-radius: 2.6vw;
        margin: 5vh auto 0px;
        overflow-y: auto;
        background: #F3F3F4;
    }

    .rest-times {
        margin-top: 8vw;
        font-size: 3.7vw;
        font-family: Source Han Sans CN;
        span {
            color: #5473E8;
            font-weight: 700;
        }
    }

    .confirm {
        margin-top: 3.9vw;
        font-family: Source Han Sans CN;
        --van-button-default-background: #5473E8;
        --van-button-normal-font-size: 3vw;
        --van-button-default-color: white;
        --van-button-normal-padding: 0px 20px;
        --van-button-default-height: 4.5vh;
    }
}
</style>