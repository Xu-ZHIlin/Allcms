<template>
  <nav class="nav">
    <ul class="rootul">
      <li class="mainmenu">
        <router-link to="/">首页</router-link>
      </li>
      <li class="mainmenu">
        <a href="#" target="_blank">学院概况</a>
        <ul class="chang">
          <li>
            <a href="#" target="_blank">学院简介</a>
          </li>
          <li>
            <a href="#" target="_blank">现任领导</a>
          </li>
          <li>
            <a href="#" target="_blank">组织机构</a>
          </li>
          <li>
            <a href="#" target="_blank">办事指南</a>
          </li>
        </ul>
      </li>

      <li class="mainmenu">
        <a href="#" target="_blank">师资队伍</a>
        <ul class="chang">
          <li>
            <a href="#" target="_blank">师资队伍简介</a>
          </li>
          <li>
            <a href="#" target="_blank">硕士博士导师</a>
          </li>
          <li>
            <a href="#" target="_blank">按教师所在部门查看</a>
          </li>
        </ul>
      </li>

      <li class="mainmenu">
        <a href="#" target="_blank">本科生教育</a>
        <ul class="chang">
          <li>
            <a href="#" target="_blank">本科生教育</a>
          </li>
          <li>
            <a href="#" target="_blank">创新创业</a>
          </li>
        </ul>
      </li>

      <li class="mainmenu">
        <a href="#" target="_blank">研究生教育</a>
        <ul class="chang">
          <li>
            <a href="#" target="_blank">学位点年度建设报告</a>
          </li>
        </ul>
      </li>

      <li class="mainmenu">
        <a href="#" target="_blank">科学研究</a>
        <ul class="chang">
          <li>
            <a href="#" target="_blank">科研平台</a>
          </li>
          <li>
            <a href="#" target="_blank">研究团队</a>
          </li>
          <li>
            <a href="#" target="_blank">科研项目</a>
          </li>
        </ul>
      </li>

      <li class="mainmenu">
        <a href="#" target="_blank">党建工作</a>
        <ul class="chang">
          <li>
            <a href="#" target="_blank">支部设置</a>
          </li>
          <li>
            <a href="#" target="_blank">专题活动</a>
          </li>
          <li>
            <a href="#" target="_blank">理论学习</a>
          </li>
          <li>
            <a href="#" target="_blank">基层动态</a>
          </li>
        </ul>
      </li>

      <li class="mainmenu">
        <a href="#" target="_blank">学生工作</a>
        <ul class="chang">
          <li>
            <a href="#" target="_blank">学生活动</a>
          </li>
          <li>
            <a href="#" target="_blank">通知公告</a>
          </li>
          <li>
            <a href="#" target="_blank">就业指导</a>
          </li>
          <li>
            <a href="#" target="_blank">易班动态</a>
          </li>
        </ul>
      </li>

      <li class="mainmenu">
        <a href="#" target="_blank">人才招聘</a>
      </li>
    </ul>
  </nav>
</template>

<script setup lang="ts">
// NavBar is stateless static markup for this application
</script>

<style scoped>
.nav {
  width: 100%vh;
  height: 50px;
  background-color: #275895;
  font-family: "微软雅黑";
}

.rootul {
  width: 140vh;
  margin: 0 auto;
  padding: 0;
  display: flex;
  justify-content: start;
}

li {
  list-style: none;
  box-sizing: border-box;
}

.rootul > li {
  width: 100px;
  height: 50px;
  line-height: 50px;
  position: relative;
}

li a {
  display: block;
  width: 100%;
  color: white;
  text-decoration: none;
  transition: background-color 0.3s;
}

/* 一级菜单的hover */
/* .rootul > li > a {
  display: block;
  width: 100%;
  height: 100%;
  color: white;
  text-decoration: none;
  transition: background-color 0.3s;
} */

.rootul > li > a:hover {
  background-color: #1d3f7e;
}

/* 二级ul */
ul.chang {
  width: 160px;
  background-color: #1d3f7e;
  max-height: 0;
  overflow: hidden;
  opacity: 0;
  transition: all 0.4s ease-in-out;
  position: absolute;
  top: 50px;
  left: 0;
  z-index: 999;
  padding: 0;
  margin: 0;
}

/* 一级的li悬停，展开二级菜单 */
.rootul > li:hover ul.chang {
  max-height: 300px;
  opacity: 1;
}

.chang > li {
  width: 100%;
  height: 38px;
  line-height: 38px;
}

.chang > li a {
  text-align: center;
  font-size: 14px;
  transition: background-color 0.3s;
}

/* 二级菜单的hover */
.chang > li a:hover {
  background-color: #275895;
}
</style>
