<template>
    <van-nav-bar title="AI咨询" class="nav-bar" />
    <AccompanyHeader />
    <RouterView />
    <NavFooter />
</template>

<script setup>
import NavFooter from '@/components/NavFooter.vue';
import AccompanyHeader from './components/AccompanyHeader.vue';
import { ref } from 'vue';

</script>


<style scoped lang="less">
.nav-bar {
    background-color: #F2F5FF;
    --van-nav-bar-icon-color: #333333;
}
</style>