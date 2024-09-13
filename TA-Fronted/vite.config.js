import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite';
import { VantResolver } from '@vant/auto-import-resolver';

// https://vitejs.dev/config/
export default defineConfig({
  base: './',
  build: {
    outDir: '../TA-UIdesign/www',
    assetsDir: 'static'
  },
  // 非本地调试时关闭代理
  // server: {
  //   proxy: {
  //     '/api': {
  //       // target: 'http://inkslab.icu:8090/', // 目标服务器地址
  //       // target: 'http://172.20.1.119:8101/', // 目标服务器地址
  //       target: 'http://172.20.1.119:8100/',  // nginx 地址
  //       changeOrigin: true,
  //       rewrite: (path) => path.replace(/^\/api/, '') // 去掉请求路径中的/api前缀
  //     }
  //   }
  // },
  plugins: [
    vue(),
    Components({
      resolvers: [VantResolver()],
    }),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  }
})
