<template>
  <div class="delete-test">
    <h3>删除小说功能测试</h3>
    
    <div class="test-section">
      <h4>测试删除API调用</h4>
      <div class="test-controls">
        <input 
          v-model="testBookId" 
          placeholder="输入要测试的小说ID" 
          type="number"
          style="padding: 8px; margin-right: 10px; border: 1px solid #ddd; border-radius: 4px;"
        />
        <button 
          @click="testDelete" 
          :disabled="!testBookId"
          style="padding: 8px 16px; background: #ff4757; color: white; border: none; border-radius: 4px; cursor: pointer;"
        >
          测试删除
        </button>
      </div>
      
      <div v-if="testResult" class="test-result" style="margin-top: 20px; padding: 10px; border-radius: 4px;" :class="testResult.success ? 'success' : 'error'">
        <h5>测试结果:</h5>
        <p><strong>状态:</strong> {{ testResult.success ? '成功' : '失败' }}</p>
        <p><strong>消息:</strong> {{ testResult.message }}</p>
        <p v-if="testResult.error"><strong>错误详情:</strong> {{ testResult.error }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue';
import { deleteBook } from '@/api/author';

export default {
  name: 'DeleteBookTest',
  setup() {
    const testBookId = ref('');
    const testResult = ref(null);

    const testDelete = async () => {
      if (!testBookId.value) return;
      
      testResult.value = null;
      
      try {
        const response = await deleteBook(testBookId.value);
        testResult.value = {
          success: true,
          message: '删除API调用成功',
          response: response
        };
      } catch (error) {
        testResult.value = {
          success: false,
          message: '删除API调用失败',
          error: error.response?.data?.msg || error.message
        };
      }
    };

    return {
      testBookId,
      testResult,
      testDelete
    };
  }
};
</script>

<style scoped>
.delete-test {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.test-section {
  border: 1px solid #ddd;
  padding: 20px;
  border-radius: 5px;
  margin-bottom: 20px;
}

.test-controls {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.test-result.success {
  background: #d4edda;
  border: 1px solid #c3e6cb;
  color: #155724;
}

.test-result.error {
  background: #f8d7da;
  border: 1px solid #f5c6cb;
  color: #721c24;
}

.test-result h5 {
  margin-top: 0;
}

.test-result p {
  margin: 5px 0;
}
</style>