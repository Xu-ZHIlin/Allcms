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
        <a href="#" target="_blank">{{categoryMap(article.category)}}</a>
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

      <!-- 上下一篇 -->
      <div class="main_art">
        <div class="wp_art_adjoin">
            <span class="prev">上一篇：</span>
            <span class="prev-title">
              <router-link v-if="prevArticle" :to="`/news/${prevArticle.id}`">
                {{ prevArticle.title }}
              </router-link>
              <template v-else>无</template>
            </span>
            <span class="next-label-spacing"></span>
            <span class="next">下一篇：</span>
            <span class="next-title">
              <router-link v-if="nextArticle" :to="`/news/${nextArticle.id}`">
                {{ nextArticle.title }}
              </router-link>
              <template v-else>无</template>
            </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { getNews, type NewsData } from '../http/news';

const route = useRoute();
const newsList = ref<NewsData[]>([]);

const categoryMap = (category:any)=>{
   switch (category) {
    case 1: return '学院新闻';
    case 2: return '通知公告';
    case 3: return '学术活动';
    case 4: return '学工新闻';
    case 5: return '党建工作';
    default: return '未知';
  }
}

onMounted(async () => {
  try {
    const res: any = await getNews();
    newsList.value = res?.data ?? res ?? [];
  } catch { }
});

const articleId = computed(() => Number(route.params.id));
const currentIndex = computed(() => newsList.value.findIndex(n => n.id === articleId.value));
const article = computed(() => newsList.value[currentIndex.value] ?? {} as NewsData);
//console.log(article.value.category)
const prevArticle = computed(() => currentIndex.value > 0 ? newsList.value[currentIndex.value - 1] : null);
const nextArticle = computed(() => currentIndex.value >= 0 && currentIndex.value < newsList.value.length - 1 ? newsList.value[currentIndex.value + 1] : null);

const fmt = (ts?: number) => ts ? new Date(ts).toLocaleDateString('zh-CN') : '';

watch(() => route.params.id, () => window.scrollTo({ top: 0, behavior: 'smooth' }));
</script>

<style scoped>
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

/* 富文本样式 */
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

.wp_art_adjoin a {
  color: #275895;
  text-decoration: none;
}

.wp_art_adjoin a:hover {
  text-decoration: underline;
}

.next-label-spacing {
  margin-left: 30px;
}

.prev, .next {
  color: #666;
}

.prev-title, .next-title {
  color: #222;
}
</style>
