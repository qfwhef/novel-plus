<template>
  <div class="image-test">
    <h3>图片显示测试</h3>
    
    <div class="test-case">
      <h4>测试用例1: 完整URL</h4>
      <p>原始URL: {{ fullUrl }}</p>
      <p>处理后URL: {{ getImageUrl(fullUrl) }}</p>
      <img :src="getImageUrl(fullUrl)" alt="完整URL测试" style="width: 100px; height: 100px; border: 1px solid #ccc;" />
    </div>
    
    <div class="test-case">
      <h4>测试用例2: 相对路径</h4>
      <p>原始URL: {{ relativePath }}</p>
      <p>处理后URL: {{ getImageUrl(relativePath) }}</p>
      <img :src="getImageUrl(relativePath)" alt="相对路径测试" style="width: 100px; height: 100px; border: 1px solid #ccc;" />
    </div>
    
    <div class="test-case">
      <h4>测试用例3: 空值</h4>
      <p>原始URL: {{ emptyUrl }}</p>
      <p>处理后URL: {{ getImageUrl(emptyUrl, defaultImage) }}</p>
      <img :src="getImageUrl(emptyUrl, defaultImage)" alt="空值测试" style="width: 100px; height: 100px; border: 1px solid #ccc;" />
    </div>
    
    <div class="test-case">
      <h4>环境变量</h4>
      <p>VUE_APP_BASE_IMG_URL: {{ baseImgUrl }}</p>
      <p>VUE_APP_BASE_API_URL: {{ baseApiUrl }}</p>
    </div>
  </div>
</template>

<script>
import { getImageUrl } from '@/utils/imageHelper';
import defaultImage from '@/assets/images/pic_upload.png';

export default {
  name: 'ImageTest',
  setup() {
    const fullUrl = 'https://wspp.min0326.top/b78130a1-b409-428f-bbc6-129bfaddd87f.jpg';
    const relativePath = '/localPic/2021/08/28/a5896a7bff2d4ae08f8c5e99355dfd06.jpg';
    const emptyUrl = '';
    
    const baseImgUrl = process.env.VUE_APP_BASE_IMG_URL;
    const baseApiUrl = process.env.VUE_APP_BASE_API_URL;
    
    return {
      fullUrl,
      relativePath,
      emptyUrl,
      defaultImage,
      baseImgUrl,
      baseApiUrl,
      getImageUrl
    };
  }
};
</script>

<style scoped>
.image-test {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.test-case {
  margin-bottom: 30px;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 5px;
}

.test-case h4 {
  margin-top: 0;
  color: #333;
}

.test-case p {
  margin: 5px 0;
  font-family: monospace;
  background: #f5f5f5;
  padding: 5px;
  border-radius: 3px;
}

.test-case img {
  margin-top: 10px;
  object-fit: cover;
}
</style>