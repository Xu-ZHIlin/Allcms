<template>
  <div class="news-detail-container">
    <!-- 面包屑导航 -->
    <div class="main_titT">
      <img
          src="https://www.guet.edu.cn/_upload/tpl/00/1d/29/template29/htmlRes/bri_icon1.png"
          alt="图标"
      />
      <span>
        <router-link to="/">首页</router-link>
        <span class="possplit"> &gt; </span>
        <a href="#" target="_blank">{{ categoryMap(article.category) }}</a>
      </span>
      <span class="possplit"> &gt; </span> 正文
    </div>

    <!-- 文章主体 -->
    <div class="main_content">
      <div class="main_contit">
        <h2>{{ article.title }}</h2>
        <p>
          供稿：{{ article.supplier || '无' }}&nbsp;&nbsp;&nbsp;&nbsp;
          时间：{{ fmt(article.publishTime) }}&nbsp;&nbsp;&nbsp;&nbsp;
        </p>
      </div>

      <div class="main_conDiv">
        <!-- 富文本内容 -->
        <div class="wp_articlecontent" v-html="article.content"></div>
      </div>

      <!-- 上下一篇（暂时没有前后文，可以隐藏或保留） -->
      <div class="main_art">
        <div class="wp_art_adjoin">
          <span class="prev">上一篇：</span>
          <span class="prev-title">
              <template>无</template>
            </span>
          <span class="next-label-spacing"></span>
          <span class="next">下一篇：</span>
          <span class="next-title">
              <template>无</template>
            </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
// 注意：这里改为引入 getNewsDetail
import { getNewsDetail } from '../http/news';

const route = useRoute();
const article = ref<any>({});
const loading = ref(false);

// 分类映射
const categoryMap = (category: string) => {
  switch (category) {
    case '学院新闻': return '学院新闻';
    case '通知公告': return '通知公告';
    case '学术活动': return '学术活动';
    case '学工新闻': return '学工新闻';
    case '党建工作': return '党建工作';
    default: return '未知';
  }
}

// 时间格式化
const fmt = (ts?: string) => {
  if (!ts) return '无';
  // 尝试兼容 ISO 时间字符串 (后端返回)
  const date = new Date(ts);
  if (isNaN(date.getTime())) return ts;
  return date.toLocaleDateString('zh-CN');
}

// 核心：根据路由参数 id 单独请求后端详情
onMounted(async () => {
  loading.value = true;
  try {
    const id = Array.isArray(route.params.id) ? route.params.id[0] : route.params.id;
    if (!id) return;

    const res: any = await getNewsDetail(id);
    // 根据 axios 拦截器逻辑，数据在 res.data 里
    article.value = res?.data || {};
  } catch (err) {
    console.error('获取详情失败：', err);
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
/* 您原本的 CSS 样式保持原封不动 */
.news-detail-container {
  width: 1000px;
  margin: 0 auto;
  min-height: 500px;
}

.main_titT {
  display: flex;
  border-bottom: 2px solid #ffb64d;
  color: #999;
  height: 55px;
  align-items: center;
  font-family: "微软雅黑";
  font-size: 14px;
}

.main_titT img {
  width: 15px;
  height: 19px;
  margin-right: 8px;
}

.main_titT a {
  text-decoration: none;
  color: #999999;
  font-size: 14px;
  padding: 0px 3px;
}

.main_titT a:hover {
  color: #275895;
  text-decoration: underline;
}

.possplit {
  padding: 0px 5px;
}

.main_content {
  border: 1px solid rgba(231, 231, 231, 0.7);
  margin-bottom: 28px;
  box-sizing: border-box;
}

.main_contit h2 {
  margin: 46px 0 0;
  text-align: center;
  font-weight: normal;
  font-family: "微软雅黑";
  font-size: 24px;
  color: #222;
}

.main_contit p {
  margin: 34px 0 10px;
  text-align: center;
  font-size: 14px;
  font-family: "微软雅黑";
  color: #999;
}

.main_conDiv {
  margin: 10px auto;
  border-top: 1px dotted rgba(231, 231, 231, 0.7);
  padding: 20px 16px;
}

:deep(.wp_articlecontent) {
  font-family: "微软雅黑";
  color: #222;
}

:deep(.wp_articlecontent p) {
  font-size: 21px;
  line-height: 1.8;
  margin-bottom: 1.5em;
  text-align: justify;
}

:deep(.wp_articlecontent .pImg) {
  text-align: center;
  margin: 20px 0;
}

:deep(.wp_articlecontent img) {
  width: 599px;
  height: 450px;
  object-fit: cover;
  border: 1px solid #ddd;
}

.main_art {
  width: 100%;
  background-color: #fbfbfb;
  margin-top: 20px;
  border: 1px solid #eee;
}

.wp_art_adjoin {
  padding: 20px 10px;
  font-family: "微软雅黑";
  font-size: 12px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.next-label-spacing {
  margin-left: 30px;
}

.prev, .next {
  color: #666;
}
</style>