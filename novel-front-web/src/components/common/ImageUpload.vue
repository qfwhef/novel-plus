<template>
  <div class="image-upload">
    <el-upload
      class="avatar-uploader"
      :action="uploadUrl"
      :show-file-list="false"
      :on-success="handleSuccess"
      :before-upload="beforeUpload"
      :on-error="handleError"
      :headers="uploadHeaders"
      :disabled="uploading"
    >
      <img v-if="displayUrl" :src="displayUrl" class="avatar" />
      <el-icon v-else class="avatar-uploader-icon">
        <Plus />
      </el-icon>
      <div v-if="uploading" class="upload-loading">
        <el-icon class="is-loading">
          <Loading />
        </el-icon>
        <span>上传中...</span>
      </div>
    </el-upload>
    
    <!-- 手动上传模式 -->
    <div v-if="manual" class="manual-upload">
      <input
        ref="fileInput"
        type="file"
        accept="image/*"
        @change="handleFileChange"
        style="display: none"
      />
      <el-button @click="selectFile" :loading="uploading">
        选择图片
      </el-button>
      <el-button 
        v-if="selectedFile" 
        type="primary" 
        @click="uploadFile"
        :loading="uploading"
      >
        上传
      </el-button>
    </div>
  </div>
</template>

<script>
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus, Loading } from '@element-plus/icons-vue';
import { uploadImage } from '@/api/resource';
import { getToken } from '@/utils/auth';

export default {
  name: 'ImageUpload',
  components: {
    Plus,
    Loading
  },
  props: {
    // 图片URL
    modelValue: {
      type: String,
      default: ''
    },
    // 是否手动上传模式
    manual: {
      type: Boolean,
      default: false
    },
    // 图片大小限制(MB)
    maxSize: {
      type: Number,
      default: 5
    },
    // 允许的图片格式
    allowedTypes: {
      type: Array,
      default: () => ['image/jpeg', 'image/png', 'image/gif']
    },
    // 图片尺寸
    width: {
      type: Number,
      default: 178
    },
    height: {
      type: Number,
      default: 178
    }
  },
  emits: ['update:modelValue', 'success', 'error'],
  setup(props, { emit }) {
    const uploading = ref(false);
    const selectedFile = ref(null);
    const fileInput = ref(null);
    
    // 智能处理图片URL显示
    const displayUrl = computed(() => {
      if (!props.modelValue) return '';
      
      // 如果是完整的HTTP/HTTPS URL，直接返回
      if (props.modelValue.startsWith('http://') || props.modelValue.startsWith('https://')) {
        return props.modelValue;
      }
      
      // 如果是相对路径，拼接基础URL
      if (props.modelValue.startsWith('/')) {
        return process.env.VUE_APP_BASE_IMG_URL + props.modelValue;
      }
      
      // 其他情况也拼接基础URL
      return process.env.VUE_APP_BASE_IMG_URL + '/' + props.modelValue;
    });
    
    const uploadUrl = computed(() => {
      return process.env.VUE_APP_BASE_API_URL + '/front/resource/image';
    });
    
    const uploadHeaders = computed(() => {
      const token = getToken();
      return token ? { 'Authorization': `Bearer ${token}` } : {};
    });
    
    // 文件验证
    const validateFile = (file) => {
      if (!props.allowedTypes.includes(file.type)) {
        ElMessage.error(`只能上传 ${props.allowedTypes.join(', ')} 格式的图片!`);
        return false;
      }
      
      if (file.size / 1024 / 1024 > props.maxSize) {
        ElMessage.error(`图片大小不能超过 ${props.maxSize}MB!`);
        return false;
      }
      
      return true;
    };
    
    // Element Upload 组件的回调
    const beforeUpload = (file) => {
      if (!validateFile(file)) return false;
      uploading.value = true;
      return true;
    };
    
    const handleSuccess = (response) => {
      uploading.value = false;
      console.log('上传响应:', response);
      
      if (response && response.data) {
        emit('update:modelValue', response.data);
        emit('success', response.data);
        ElMessage.success('上传成功!');
      } else {
        ElMessage.error('上传失败: 响应格式错误');
        emit('error', response);
      }
    };
    
    const handleError = (error) => {
      uploading.value = false;
      console.error('上传错误:', error);
      ElMessage.error('上传失败!');
      emit('error', error);
    };
    
    // 手动上传模式的方法
    const selectFile = () => {
      fileInput.value.click();
    };
    
    const handleFileChange = (event) => {
      const file = event.target.files[0];
      if (file && validateFile(file)) {
        selectedFile.value = file;
      }
    };
    
    const uploadFile = async () => {
      if (!selectedFile.value) return;
      
      uploading.value = true;
      try {
        const response = await uploadImage(selectedFile.value);
        if (response && response.data) {
          emit('update:modelValue', response.data);
          emit('success', response.data);
          ElMessage.success('上传成功!');
          selectedFile.value = null;
          fileInput.value.value = '';
        } else {
          throw new Error('上传失败: 响应格式错误');
        }
      } catch (error) {
        console.error('手动上传错误:', error);
        ElMessage.error(error.message || '上传失败!');
        emit('error', error);
      } finally {
        uploading.value = false;
      }
    };
    
    return {
      uploading,
      selectedFile,
      fileInput,
      displayUrl,
      uploadUrl,
      uploadHeaders,
      beforeUpload,
      handleSuccess,
      handleError,
      selectFile,
      handleFileChange,
      uploadFile
    };
  }
};
</script>

<style scoped>
.image-upload {
  display: inline-block;
}

.avatar-uploader .avatar {
  width: v-bind(width + 'px');
  height: v-bind(height + 'px');
  display: block;
  object-fit: cover;
  border-radius: 6px;
}

.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: v-bind(width + 'px');
  height: v-bind(height + 'px');
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  text-align: center;
}

.upload-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.8);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: var(--el-color-primary);
}

.manual-upload {
  margin-top: 10px;
  text-align: center;
}

.manual-upload .el-button + .el-button {
  margin-left: 10px;
}
</style>