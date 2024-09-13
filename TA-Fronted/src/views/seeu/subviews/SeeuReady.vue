<template>
    <div class="seeuready">
        <div class="header">点击下方按钮开启对话</div>
        <div class="button" @click="startSeeu"></div>
    </div>
</template>

<script setup>
import router from '@/router';
import buttonImg from '@/assets/meetwithTA.png';

const startSeeu = () => {
    router.replace('/seeumain')
}

</script>

<style scoped lang="less">
.seeuready {
    display: flex;
    flex-direction: column;
    justify-content: center;
    flex-wrap: wrap;
    align-content: center;
    width: 100vw;
    height: 100vh;
    .header {
        text-align: center;
    }

    .button {
        background-image: url('@/assets/meetwithTA.png');
        background-size: cover;
        width: 100vw;
        height: 100vw;
    }
}

</style>