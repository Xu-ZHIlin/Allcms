import axios from "./axios";

//后端数据库中新闻表的字段
export interface NewsData {
  id?: number;
  title?: string; //新闻标题
  category?: number; //新闻栏目(1学院新闻，2通知公告，3学术活动，4学工新闻，5党建工作)
  content?: string; //新闻内容
  supplier?: string; //供稿人
  reviewer?: string; //审稿人
  status?: string; //新闻稿件状态
  createTime?: number; //创建时间
  publishTime?: number; //发布时间
  updateTime?: number; //修改时间
}

//获取新闻列表
export const getNews = () => {
    return axios({
        url: '/portal/news/getPublishedNews',
        method: 'GET',
    })
};