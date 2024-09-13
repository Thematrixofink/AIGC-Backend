import { defineStore } from "pinia";
import COS from "cos-js-sdk-v5";

const useCosStore = defineStore('cosstore', () => {
    // 创建COS对象时必须传入getAuthorization函数，每次调用cos方法时都通过该函数获得密钥
    // 或者通过以下方式放入静态密钥
    var cos = new COS({
        SecretId: 'AKID7EE93yiGg4JSQCan2999qS0OWXVN7ct6',
        SecretKey: 'CD250jGbmjYSIxdliUAkJfOzVpukZDgw',
    });
    
    // 获取函数
    const getUrl = (key) => {
        let res = cos.getObjectUrl({
            Bucket: "original-1317028174",
            Region: "ap-beijing",
            Key: key
        })
        return res;
    }

    return { cos, getUrl }
})



export default useCosStore;