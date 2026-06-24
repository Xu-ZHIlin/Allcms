export interface SliderItem {
  id: number;
  img: string;
  title: string;
  link: string;
}

export interface NewsItem {
  id: number;
  title: string;
  shortTitle?: string;
  date: string;
}

export interface FeatureItem {
  id: number;
  img: string;
  title: string;
}

export interface LinkItem {
  name: string;
  url: string;
}

export interface NewsArticle {
  title: string;
  author: string;
  time: string;
  clicks: number;
  content: string;
  prevId: number | null;
  nextId: number | null;
}

// 新闻与通知公告等数据源
export const sliderItems: SliderItem[] = [
  {
    id: 1,
    img: "https://www.guet.edu.cn/_upload/article/images/76/ad/3e00fd314f5a886d2045a2f39107/1991725f-6149-480c-b0c4-a3172a84bb08_s.jpg",
    title: "数学与计算科学学院举行新进教职工入职仪式（图）",
    link: "/news/1"
  },
  {
    id: 2,
    img: "https://www.guet.edu.cn/_upload/article/images/76/ad/3e00fd314f5a886d2045a2f39107/1991725f-6149-480c-b0c4-a3172a84bb08_s.jpg",
    title: "袁亚湘院士指导我院学科建设与科学研究",
    link: "/news/9"
  },
  {
    id: 3,
    img: "https://www.guet.edu.cn/_upload/article/images/76/ad/3e00fd314f5a886d2045a2f39107/1991725f-6149-480c-b0c4-a3172a84bb08_s.jpg",
    title: "2021年数学与计算科学学院党员学习活动",
    link: "/news/10"
  },
  {
    id: 4,
    img: "https://www.guet.edu.cn/_upload/article/images/76/ad/3e00fd314f5a886d2045a2f39107/1991725f-6149-480c-b0c4-a3172a84bb08_s.jpg",
    title: "学院师生积极参加2021年校运动会并获佳绩",
    link: "/news/11"
  },
  {
    id: 5,
    img: "https://www.guet.edu.cn/_upload/article/images/76/ad/3e00fd314f5a886d2045a2f39107/1991725f-6149-480c-b0c4-a3172a84bb08_s.jpg",
    title: "数学与计算科学学院实验室及仪器设备概况",
    link: "/news/12"
  },
  {
    id: 6,
    img: "https://www.guet.edu.cn/_upload/article/images/76/ad/3e00fd314f5a886d2045a2f39107/1991725f-6149-480c-b0c4-a3172a84bb08_s.jpg",
    title: "学院第十四届团委学生会干部合影留念",
    link: "/news/13"
  }
];

export const collegeNews: NewsItem[] = [
 
];

export const announcements: NewsItem[] = [

];

export const academicActivities: NewsItem[] = [
 
];

export const partyWork: NewsItem[] = [
 
];

export const studentNews: NewsItem[] = [
 
];



export const friendLinks: LinkItem[] = [
  { name: "书记信箱: @", url: "#" },
  { name: "院长信箱: @", url: "#" },
  { name: "举报信箱: @", url: "#" },
  { name: "桂林电子科技大学", url: "#" },
  { name: "学校本科教学服务网", url: "#" },
  { name: "研究生院", url: "#" }
];

export const selectLinks: LinkItem[] = [
  { name: "清华大学", url: "https://www.tsinghua.edu.cn" },
  { name: "北京大学", url: "https://www.pku.edu.cn" },
  { name: "厦门大学", url: "https://www.xmu.edu.cn" },
  { name: "复旦大学", url: "https://www.fudan.edu.cn" },
  { name: "上海大学", url: "https://www.shu.edu.cn" }
];

// 详细新闻内容字典
export const newsArticles: Record<number, NewsArticle> = {
 
};
