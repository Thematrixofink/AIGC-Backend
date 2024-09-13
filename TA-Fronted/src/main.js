import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import piniaPersist from 'pinia-plugin-persist'
import axios from 'axios'
// import './mock/index'

const app = createApp(App)
const pinia = createPinia()
pinia.use(piniaPersist)

app.use(router)
app.use(pinia)
app.mount('#app')



const onDeviceReady = () => {
    StatusBar.overlaysWebView(false);
    StatusBar.backgroundColorByName("red");
}




// 设置基地址
const baseURL = 'http://172.20.1.119:8100';

// 创建axios实例
const instance = axios.create({
  baseURL,
});

// 发送POST请求到指定接口
const sendData = async () => {
  try {
    const response = await instance.post('/api/user/aaa');
    console.log('请求成功:', response.data);
  } catch (error) {
    console.error('请求失败:', error);
  }
};

// 调用发送请求的函数
sendData();


document.addEventListener("deviceready", onDeviceReady, false)