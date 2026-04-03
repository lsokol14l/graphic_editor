import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ImageRedactorView from '../views/ImageRedactorView.vue'
import GradationalTransformationsView from '../views/GradationalTransformationsView.vue'
import HistogramView from '../views/HistogramView.vue'

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/image-redactor',
    name: 'image-redactor',
    component: ImageRedactorView
  },
  {
    path: '/gradational-transformations',
    name: 'gradational-transformations',
    component: GradationalTransformationsView
  },
  {
    path: '/histogram',
    name: 'histogram',
    component: HistogramView
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
