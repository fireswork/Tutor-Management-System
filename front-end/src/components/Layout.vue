<template>
  <a-layout class="app-layout">
    <!-- 顶部栏 -->
    <a-layout-header class="header">
      <div class="header-left">
        <div class="logo">
          <h1>家教管理系统</h1>
        </div>
        <menu-fold-outlined
          v-if="!collapsed"
          class="trigger"
          @click="collapsed = true"
        />
        <menu-unfold-outlined
          v-else
          class="trigger"
          @click="collapsed = false"
        />
      </div>
      <div class="header-right">
        <a-dropdown>
          <div class="user-info">
            <a-avatar :size="32">{{ userRole === "ADMIN" ? "管" : userRole === "USER" ? "用" : "师" }}</a-avatar>
            <span class="username">{{ userName || "用户" }}</span>
          </div>
          <template #overlay>
            <a-menu>
              <a-menu-item key="profile">
                <router-link to="/user/profile">个人中心</router-link>
              </a-menu-item>
              <a-menu-item key="logout" @click="handleLogout">
                退出登录
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
    </a-layout-header>

    <a-layout class="site-layout">
      <!-- 侧边栏 -->
      <a-layout-sider
        v-model:collapsed="collapsed"
        :trigger="null"
        collapsible
        class="sidebar"
        width="240"
      >
        <div class="sider-menu">
          <a-menu
            v-model:selectedKeys="selectedKeys"
            v-model:openKeys="openKeys"
            theme="dark"
            mode="inline"
          >
            <a-menu-item key="course-list">
              <template #icon><book-outlined /></template>
              <span>课程管理</span>
              <router-link to="/course-list"></router-link>
            </a-menu-item>

            <a-sub-menu key="user-center">
              <template #icon><user-outlined /></template>
              <template #title>个人中心</template>
              <a-menu-item key="user-profile">
                <template #icon><IdcardOutlined /></template>
                <router-link to="/user/profile">个人信息</router-link>
              </a-menu-item>
              <a-menu-item key="user-qualifications">
                <template #icon><SafetyCertificateOutlined /></template>
                <router-link to="/user/qualifications">资质管理</router-link>
              </a-menu-item>
            </a-sub-menu>

            <a-menu-item key="order-manage">
              <template #icon><shopping-outlined /></template>
              <span>订单管理</span>
              <router-link to="/order-manage"></router-link>
            </a-menu-item>

            <a-menu-item key="review-manage">
              <template #icon><solution-outlined /></template>
              <span>评价管理</span>
              <router-link to="/review-manage"></router-link>
            </a-menu-item>

            <a-menu-item key="user-management">
              <template #icon><UsergroupAddOutlined /></template>
              <router-link to="/users">用户管理</router-link>
            </a-menu-item>
            <a-menu-item key="teacher-management">
              <template #icon><TeamOutlined /></template>
              <router-link to="/teachers">教师管理</router-link>
            </a-menu-item>

            <template>
              <a-menu-item key="teacher-center">
                <template #icon><solution-outlined /></template>
                <span>教师中心</span>
                <router-link to="/teacher-center"></router-link>
              </a-menu-item>
              <a-menu-item key="course-manage">
                <template #icon><schedule-outlined /></template>
                <span>课程管理</span>
                <router-link to="/course-manage"></router-link>
              </a-menu-item>
            </template>
          </a-menu>
        </div>
      </a-layout-sider>

      <!-- 内容区域 -->
      <a-layout-content class="content">
        <div class="content-inner">
          <slot></slot>
        </div>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  TeamOutlined,
  BookOutlined,
  UserOutlined,
  ShoppingOutlined,
  SolutionOutlined,
  ScheduleOutlined,
  CrownOutlined,
  UsergroupAddOutlined,
  ReadOutlined,
  IdcardOutlined,
  SafetyCertificateOutlined,
} from "@ant-design/icons-vue";
import { message } from "ant-design-vue";

const router = useRouter();
const route = useRoute();
const collapsed = ref(false);
const selectedKeys = ref(["teacher-list"]);
const openKeys = ref(["user-center"]);

// 处理路由变化的函数
const handleRouteChange = (path) => {
  const routeMap = {
    "/teacher-list": { selected: ["teacher-list"] },
    "/course-list": { selected: ["course-list"] },
    "/user/profile": { selected: ["user-profile"], open: ["user-center"] },
    "/user/qualifications": {
      selected: ["user-qualifications"],
      open: ["user-center"],
    },
    "/order-manage": { selected: ["order-manage"] },
    "/review-manage": { selected: ["review-manage"] },
    "/teacher-center": { selected: ["teacher-center"] },
    "/course-manage": { selected: ["course-manage"] },
    "/users": { selected: ["user-management"] },
    "/teachers": { selected: ["teacher-management"] },
    "/subjects": { selected: ["subject-management"] },
  };

  for (const [routePath, keys] of Object.entries(routeMap)) {
    if (path.includes(routePath)) {
      selectedKeys.value = keys.selected;
      if (keys.open) {
        openKeys.value = keys.open;
      }
      break;
    }
  }
};

// 监听路由变化
watch(() => route.path, handleRouteChange, { immediate: true });

// 用户信息
const isLoggedIn = ref(!!localStorage.getItem("token"));
const userName = ref(localStorage.getItem("userName") || "");
const userRole = ref(localStorage.getItem("userRole") || "");

onMounted(() => {
  handleRouteChange(route.path);
});

// 退出登录
const handleLogout = () => {
  localStorage.removeItem("token");
  localStorage.removeItem("userName");
  localStorage.removeItem("isTeacher");
  localStorage.removeItem("userRole");
  isLoggedIn.value = false;
  message.success("退出登录成功");
  router.push({ name: "Login" });
};
</script>

<style lang="less" scoped>
@import "../assets/styles/variables.less";

.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 24px;
    background: #001529;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
    height: 64px;
    line-height: 64px;
    z-index: 100;

    .header-left {
      display: flex;
      align-items: center;

      .trigger {
        font-size: 20px;
        padding: 0 24px;
        cursor: pointer;
        transition: color 0.3s;
        color: #fff;

        &:hover {
          color: @primary-color;
        }
      }

      .logo {
        h1 {
          color: white;
          font-size: 18px;
          margin: 0;
          white-space: nowrap;
        }
      }
    }

    .header-right {
      .user-info {
        display: flex;
        align-items: center;
        cursor: pointer;
        color: white;

        .username {
          margin-left: 8px;
        }
      }
    }
  }

  .site-layout {
    display: flex;
    flex: 1;

    .sidebar {
      position: relative;
      height: calc(100vh - 64px);
      overflow: auto;

      .sider-menu {
        height: 100%;
        overflow-y: auto;
      }
    }

    .content {
      padding: 24px;
      background: #f0f2f5;
      height: calc(100vh - 64px);
      overflow: auto;

      .content-inner {
        background: white;
        padding: 24px;
        border-radius: 4px;
        min-height: calc(100% - 48px);
      }
    }
  }
}
</style>
