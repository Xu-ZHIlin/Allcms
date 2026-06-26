<template>
  <div>
    <!-- 横幅 -->
    <div id="top3">
      <img
          src="https://www.guet.edu.cn/_upload/article/images/be/2d/7cfb63a54f26ac8315a7a1d0459b/e2290421-481e-4f2f-8d27-0839a4409a8b.jpg"
          width="1000"
          height="200"
          alt="学院横幅"
      />
    </div>

    <!-- 内容大区域 -->
    <div class="container">
      <!-- 第1大行（左中右） -->
      <div class="content">
        <!-- 左：学院新闻 -->
        <div class="informBox">
          <div class="dynamic">
            <h2>学院新闻</h2>
            <span>
              <a href="#" target="_blank">
                <img src="https://www.guet.edu.cn/_upload/tpl/00/1d/29/template29/htmlRes/w_more.png" alt="更多" />
              </a>
            </span>
          </div>
          <div class="infor_list">
            <ul class="dynamic_list">
              <li v-for="news in collegeNews" :key="news.id">
                <router-link :to="`/news/${news.id}`">
                  {{ news.title }}
                </router-link>
                <span>{{ fmt(news.publishTime) }}</span>
              </li>
            </ul>
          </div>
        </div>

        <!-- 中：图片轮播区 -->
        <div class="informBox">
          <div class="informBoxM" @mouseenter="stopSlider" @mouseleave="startSlider">
            <div class="dtjt9_div_img">
              <router-link :to="sliderItems[currentIndex].link">
                <img
                    id="w5imgShow"
                    name="w5imgShow"
                    :src="sliderItems[currentIndex].img"
                    :alt="sliderItems[currentIndex].title"
                />
              </router-link>
              <div id="w5imgNumber" class="number">
                <a
                    href="javascript:void(0)"
                    v-for="(item, idx) in sliderItems"
                    :key="item.id"
                    :class="currentIndex === idx ? 'current' : 'nomal'"
                    @mouseenter="setCurrentIndex(idx)"
                >
                  {{ idx + 1 }}
                </a>
              </div>
            </div>
            <div class="dtjt9_div_text">
              <div id="w5imgTitle" class="dtjt9_div_text_title">
                <router-link :to="sliderItems[currentIndex].link">
                  {{ sliderItems[currentIndex].title }}
                </router-link>
              </div>
            </div>
          </div>
        </div>

        <!-- 右：通知公告 -->
        <div class="informBox">
          <div class="dynamic">
            <h2>通知公告</h2>
            <span>
              <a href="https://www.guet.edu.cn/dept7/1140/list.htm" target="_blank">
                <img src="https://www.guet.edu.cn/_upload/tpl/00/1d/29/template29/htmlRes/w_more.png" alt="更多" />
              </a>
            </span>
          </div>
          <div class="infor_list">
            <ul class="dynamic_list">
              <li v-for="item in announcements" :key="item.id">
                <router-link :to="`/news/${item.id}`">
                  {{ item.title }}
                </router-link>
                <span>{{ fmt(item.publishTime) }}</span>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <!-- 第2大行（左中右） -->
      <div class="content">
        <!-- 左：学术活动 -->
        <div class="informBox">
          <div class="dynamic">
            <h2>学术活动</h2>
            <span>
              <a href="https://www.guet.edu.cn/dept7/1141/list.htm" target="_blank">
                <img src="https://www.guet.edu.cn/_upload/tpl/00/1d/29/template29/htmlRes/w_more.png" alt="更多" />
              </a>
            </span>
          </div>
          <div class="infor_list">
            <ul class="dynamic_list">
              <li v-for="item in academicActivities" :key="item.id">
                <router-link :to="`/news/${item.id}`">
                  {{ item.title }}
                </router-link>
                <span>{{ fmt(item.publishTime) }}</span>
              </li>
            </ul>
          </div>
        </div>

        <!-- 中：党建工作 -->
        <div class="informBox">
          <div class="dynamic">
            <h2>党建工作</h2>
            <span>
              <a href="https://www.guet.edu.cn/dept7/1130/list.htm" target="_blank">
                <img src="https://www.guet.edu.cn/_upload/tpl/00/1d/29/template29/htmlRes/w_more.png" alt="更多" />
              </a>
            </span>
          </div>
          <div class="infor_list">
            <ul class="dynamic_list">
              <li v-for="item in partyWork" :key="item.id">
                <router-link :to="`/news/${item.id}`" :title="item.title">
                  {{ item.title }}
                </router-link>
                <span>{{ fmt(item.publishTime) }}</span>
              </li>
            </ul>
          </div>
        </div>

        <!-- 右：学工新闻 -->
        <div class="informBox">
          <div class="dynamic">
            <h2>学工新闻</h2>
            <span>
              <a href="https://www.guet.edu.cn/dept7/1134/list.htm" target="_blank">
                <img src="https://www.guet.edu.cn/_upload/tpl/00/1d/29/template29/htmlRes/w_more.png" alt="更多" />
              </a>
            </span>
          </div>
          <div class="infor_list">
            <ul class="dynamic_list">
              <li v-for="item in studentNews" :key="item.id">
                <router-link :to="`/news/${item.id}`" :title="item.title">
                  {{ item.title }}
                </router-link>
                <span>{{ fmt(item.publishTime) }}</span>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <!-- 友情链接区域 -->
      <div class="xiaonews5">
        <div class="tit">
          <h3><span>友情链接</span></h3>
        </div>
        <div class="con">
          <div>
            <div>
              <div class="tempWrap">
                <table>
                  <tbody>
                  <tr>
                    <template v-for="(link, index) in friendLinks" :key="link.name">
                      <td class="hhh" v-if="index > 0">|</td>
                      <td>
                        <a class="c163284" :href="link.url" target="_blank">
                          {{ link.name }}
                        </a>
                      </td>
                    </template>
                  </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import { getNews, type NewsData } from '../http/news';
import {
  sliderItems,
  friendLinks
} from '../data/newsData';

// 从接口获取的新闻列表
const newsList = ref<NewsData[]>([]);

// 按 category 过滤，publishTime 格式化
const fmt = (ts?: string) => ts ? new Date(ts).toLocaleDateString('zh-CN', { month: 'numeric', day: 'numeric' }) : '';

// 【重点修改】：栏目分类从原来的数字（1,2,3...）改为后端返回的中文字符串匹配
const collegeNews        = computed(() => newsList.value.filter(n => n.category === '学院新闻').slice(0, 10));
const announcements      = computed(() => newsList.value.filter(n => n.category === '通知公告').slice(0, 10));
const academicActivities = computed(() => newsList.value.filter(n => n.category === '学术活动').slice(0, 10));
const studentNews        = computed(() => newsList.value.filter(n => n.category === '学工新闻').slice(0, 10));
const partyWork          = computed(() => newsList.value.filter(n => n.category === '党建工作').slice(0, 10));

onMounted(async () => {
  try {
    const res: any = await getNews();
    console.log('新闻接口返回数据：', res);
    // 后端真正返回的数据在 res.data.records 里面
    newsList.value = res?.data?.records ?? res ?? [];
  } catch (err) {
    console.error('获取新闻数据失败：', err);
  }
  startSlider();
});

// 轮播逻辑
const currentIndex = ref(0);
let timer: ReturnType<typeof setInterval> | null = null;

const startSlider = () => {
  stopSlider();
  timer = setInterval(() => {
    currentIndex.value = (currentIndex.value + 1) % sliderItems.length;
  }, 4000);
};

const stopSlider = () => {
  if (timer) { clearInterval(timer); timer = null; }
};

const setCurrentIndex = (index: number) => { currentIndex.value = index; };

onBeforeUnmount(() => stopSlider());
</script>

<style scoped>
#top3 img {
  display: block;
  width: 1000px;
  height: 200px;
}

.container {
  display: flex;
  flex-direction: column;
  min-height: 800px;
  width: 1000px;
  margin: 0 auto;
}

.content {
  display: flex;
  gap: 16px;
  margin-top: 20px;
}

.informBox {
  flex: 1;
  width: 320px;
  box-sizing: border-box;
}

.dynamic {
  display: flex;
  justify-content: space-between;
  width: 320px;
  height: 44px;
  background-color: #08599a;
  box-sizing: border-box;
}

.dynamic h2 {
  font-family: "微软雅黑";
  color: #ffffff;
  font-weight: normal;
  font-size: 16px;
  margin: auto 0;
  margin-left: 16px;
}

.dynamic img {
  margin-top: 30px;
  margin-right: 5px;
}

.infor_list {
  margin-top: 7px;
  border: 1px solid rgba(225, 225, 225, 0.9);
  height: 250px;
}

.dynamic_list {
  display: flex;
  flex-direction: column;
  font-family: "微软雅黑";
  font-size: 14px;
  width: 100%;
  height: 100%;
  padding: 8px 0;
  box-sizing: border-box;
  margin: 0;
}

.dynamic_list > li {
  list-style: none;
  display: flex;
  justify-content: start;
  align-items: center;
  padding: 0 3px;
  height: 28px;
}
.dynamic_list > li::before {
  content: "▪";
  color: #434343;
}
.dynamic_list a {
  display: block;
  color: #222;
  text-decoration: none;
  width: 230px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-left: 6px;
}

.dynamic_list a:hover {
  color: #08599a;
}

.dynamic_list span {
  margin-left: 50px;
  display: block;
  color: #666666;
  font-size: 16px;
}

.informBox .informBoxM {
  position: relative;
  overflow: hidden;
  width: 320px;
  height: 300px;
  box-sizing: border-box;
}

.dtjt9_div_img {
  position: relative;
  width: 320px;
  height: 271px;
}

.dtjt9_div_img img {
  width: 320px;
  height: 271px;
  object-fit: cover;
  display: block;
}

.dtjt9_div_img .number {
  position: absolute;
  right: 5px;
  bottom: 4px;
  display: flex;
  z-index: 10;
}

#w5imgNumber a {
  display: inline-flex;
  width: 17.6px;
  height: 17.6px;
  font-size: 12px;
  font-family: "微软雅黑";
  color: #ffffff;
  opacity: 0.7;
  text-decoration: none;
  border: 1px solid #fff;
  background-color: #6c6d6e;
  justify-content: center;
  align-items: center;
  margin-right: 5px;
  border-radius: 0;
  cursor: pointer;
  box-sizing: border-box;
}

#w5imgNumber a.current {
  background-color: red;
  opacity: 0.9;
  font-weight: bold;
}

.dtjt9_div_text {
  width: 320px;
  height: 27px;
  display: flex;
  align-items: center;
}

.dtjt9_div_text_title {
  margin: 0 10px;
  width: 300px;
}

.dtjt9_div_text_title a {
  display: block;
  text-decoration: none;
  font-family: "微软雅黑";
  font-size: 16px;
  color: #222;
  font-weight: bold;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.dtjt9_div_text_title a:hover {
  color: #08599a;
}

.endContent {
  display: flex;
  flex-direction: column;
  margin-top: 20px;
}

.endContent .dynamic {
  width: 1000px;
}

.gundong {
  margin-top: 7px;
  border: 1px solid rgba(225, 225, 225, 0.9);
  padding: 10px;
  width: 1000px;
  box-sizing: border-box;
}

.gundong .scroll ul {
  display: flex;
  padding: 0;
  margin: 0;
  gap: 15px;
  width: 100%;
  overflow-x: auto;
  list-style: none;
}

.gundong .scroll li {
  flex-shrink: 0;
  width: 150px;
  text-align: center;
}

.gundong .scroll img {
  width: 150px;
  height: 112px;
  object-fit: cover;
  border: 1px solid #eee;
  display: block;
}

.gundong .scroll img:hover {
  opacity: 0.85;
}

li .btt {
  width: 150px;
  height: 26px;
  line-height: 26px;
  color: #222222;
  font-family: "微软雅黑";
  font-size: 13px;
  text-align: center;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-top: 5px;
}

.xiaonews5 {
  margin-top: 20px;
  width: 1000px;
}

.xiaonews5 .tit {
  width: 100%;
  padding: 10px 0px;
  border-bottom: 2px solid #08599a;
}

.xiaonews5 .tit h3 {
  color: #000;
  font-family: "微软雅黑";
  font-size: 18px;
  font-weight: normal;
  margin: 0;
}

.xiaonews5 .con {
  margin-top: 10px;
  margin-bottom: 40px;
}

.hhh {
  width: 24px;
  text-align: center;
  color: #ccc;
  font-size: 12px;
}

.tempWrap table {
  width: 100%;
  border-collapse: collapse;
}

.tempWrap table a {
  text-decoration: none;
  color: #222222;
  font-family: "微软雅黑";
  font-size: 12px;
}

.tempWrap table a:hover {
  color: #08599a;
  text-decoration: underline;
}
</style>