import { createRouter, createWebHistory } from "vue-router";

// 懒加载路由
const Login = () => import("../views/Login.vue");
const Register = () => import("../views/Register.vue");
const CourseList = () => import("../views/CourseList.vue");
const CourseDetail = () => import("../views/CourseDetail.vue");
const TeacherDetail = () => import("../views/TeacherDetail.vue");
const UserProfile = () => import("../views/user/UserProfile.vue");
const UserQualifications = () => import("../views/user/UserQualifications.vue");
const TeacherCenter = () => import("../views/TeacherCenter.vue");
// const CourseManage = () => import('../views/CourseManage.vue')
const OrderManage = () => import("../views/OrderList.vue");
const ReviewManage = () => import("../views/ReviewManage.vue");
const UserManagement = () => import("../views/admin/UserManagement.vue");
const TeacherManagement = () => import("../views/admin/TeacherManagement.vue");
const SubjectManagement = () => import("../views/admin/SubjectManagement.vue");

const routes = [
  {
    path: "/",
    redirect: "/login",
  },
  {
    path: "/login",
    name: "Login",
    component: Login,
    meta: { title: "登录", noAuth: true },
  },
  {
    path: "/register",
    name: "Register",
    component: Register,
    meta: { title: "注册", noAuth: true },
  },
  {
    path: "/course-list",
    name: "CourseList",
    component: CourseList,
    meta: { title: "课程列表", requireAuth: false },
  },
  {
    path: "/course/:id",
    name: "CourseDetail",
    component: CourseDetail,
    meta: { title: "课程详情", requireAuth: false },
  },
  {
    path: "/teacher-detail/:id",
    name: "TeacherDetail",
    component: TeacherDetail,
    meta: { title: "教师详情", requireAuth: false },
  },
  {
    path: "/user",
    name: "UserCenter",
    meta: { title: "个人中心", requireAuth: false },
    children: [
      {
        path: "profile",
        name: "UserProfile",
        component: UserProfile,
        meta: { title: "个人信息", requireAuth: false },
      },
      {
        path: "qualifications",
        name: "UserQualifications",
        component: UserQualifications,
        meta: { title: "资质管理", requireAuth: false, requireTeacher: false },
      },
    ],
  },
  {
    path: "/teacher-center",
    name: "TeacherCenter",
    component: TeacherCenter,
    meta: { title: "教师中心", requireAuth: false, requireTeacher: true },
  },
  // {
  //   path: '/course-manage',
  //   name: 'CourseManage',
  //   component: CourseManage,
  //   meta: { title: '课程管理', requireAuth: false, requireTeacher: true }
  // },
  {
    path: "/order-manage",
    name: "OrderManage",
    component: OrderManage,
    meta: { title: "订单管理", requireAuth: false },
  },
  {
    path: "/review-manage",
    name: "ReviewManage",
    component: ReviewManage,
    meta: {
      title: "评价管理",
    },
  },
  {
    path: "/users",
    name: "UserManagement",
    component: UserManagement,
    meta: { title: "用户管理" },
  },
  {
    path: "/teachers",
    name: "TeacherManagement",
    component: TeacherManagement,
    meta: { title: "教师管理" },
  },
  {
    path: "/subjects",
    name: "SubjectManagement",
    component: SubjectManagement,
    meta: { title: "科目管理" },
  },
  {
    path: '/teachers/:teacherId/qualifications',
    name: 'TeacherQualificationReview',
    component: () => import('../views/admin/TeacherQualificationReview.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 全局前置守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title
    ? `${to.meta.title} - 家教管理系统`
    : "家教管理系统";

  // 登录验证逻辑
  const token = localStorage.getItem("token");
  const userRole = localStorage.getItem("userRole");

  next();
});

export default router;
