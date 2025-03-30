<script setup>
import Layout from "./components/Layout.vue";
import { useRoute } from "vue-router";
import zhCN from 'ant-design-vue/es/locale/zh_CN';

const route = useRoute();
const noLayoutRoutes = ["Login", "Register"];
</script>

<template>
  <!-- 对登录和注册页面不使用Layout -->
  <template v-if="noLayoutRoutes.includes(route.name)">
    <router-view />
  </template>
  <!-- 其他页面使用Layout -->
  <template v-else>
    <Layout>
      <a-config-provider :locale="zhCN">
        <router-view />
      </a-config-provider>
    </Layout>
  </template>
</template>

<style>
#app {
  font-family: "PingFang SC", "Helvetica Neue", Helvetica, "microsoft yahei",
    arial, STHeiTi, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: rgba(0, 0, 0, 0.85);
}

body {
  margin: 0;
  padding: 0;
}
</style>
