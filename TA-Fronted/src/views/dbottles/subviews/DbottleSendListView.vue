<!-- 漂流瓶列表 -->
<template>
    <van-list>
        <ChatItem v-for="(item, index) in catchedList" :key="index" :userName="item.pickedUserName"
            :userAvatar="item.pickedUserAvatar" :text="item.content"
            @click="enterBottleChat(item.bottleId, item.userName)" />
    </van-list>
    <!-- <div class="not-received" >
        暂未被接收v-if="unCatchedList.length > 0"
    </div> -->
    <van-divider :style="{ color: '#CC2525', borderColor: '#CC2525', padding: '0 16px' }" style="font-size: 12px;"
        v-if="unCatchedList.length > 0">
        暂未被接收
    </van-divider>
    <van-list>
        <ChatItem v-for="(item, index) in unCatchedList" :key="index" :userName="item.userName"
            :userAvatar="item.userAvatar" :text="item.content" />
    </van-list>
</template>

<script setup>
import { useRouter } from 'vue-router';
import ChatItem from '@/components/ChatItem.vue';
import useBottleStore from '@/stores/bottle';

const router = useRouter()
const BottleStore = useBottleStore()

// 获取信息
const catchedList = BottleStore.processRecords(BottleStore.bottleListSendCatched)
const unCatchedList = BottleStore.processRecords(BottleStore.bottleListSendUncatched)
/*catchList数据格式
{
    bottleId: e.id, // 瓶子id
    content: e.content,    // 内容
    userId: e.userId,      // 发送瓶子人id
    userAvatar: e.userAvatar,//发瓶人头像
    userName: e.userName,   // 发瓶人名称
    pickUserId: e.pickUserId,// 捞瓶人id
    pickUserAvatar: e.pickUserAvatar,// 捞瓶人头像
    pickUserName: e.pickUserName    // 捞瓶人名称
}
*/


const enterBottleChat = (id, name) => {
    router.push({
        path: '/dbottlechat',
        query: {
            id: id,
            name: name
        }
    })
}

</script>

<style scoped lang="less"></style>