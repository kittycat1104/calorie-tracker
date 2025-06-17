<template>
  <div class="home">
    <h1>歡迎回來！熱量管理系統</h1>
    <div v-if="isLoggedIn">
      <button class="advice-btn" @click="getAdvice" :disabled="adviceLoading">
        {{ adviceLoading ? '取得建議中...' : '獲取飲食建議' }}
      </button>
      <div v-if="adviceHtml" class="advice-box">
        <div v-html="adviceHtml"></div>
      </div>
    </div>
    <div v-else>
      <p>請登入以使用完整功能</p>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import MarkdownIt from 'markdown-it'
import { useUserStore } from '../stores/user'
import { ref, computed, onMounted, watch } from 'vue'

export default {
  name: 'HomeView',
  setup() {
    const userStore = useUserStore()
    const adviceLoading = ref(false)
    const todayCalories = ref(0)
    const targetCalories = ref(2000)
    const advice = computed(() => userStore.advice)
    const adviceHtml = computed(() => userStore.adviceHtml)
    const isLoggedIn = ref(false)

    onMounted(() => {
      if (!userStore.token && localStorage.getItem('token')) {
        userStore.setToken(localStorage.getItem('token'))
      }
      isLoggedIn.value = !!userStore.token
    })

    watch(() => userStore.token, (val) => {
      isLoggedIn.value = !!val
    })

    async function getAdvice() {
      adviceLoading.value = true
      try {
        const response = await axios.get('http://localhost:8081/api/nutrition/diet-detailed-advice', {
          headers: {
            'Authorization': `Bearer ${userStore.token}`
          }
        })
        const adviceText = response.data.advice || response.data.建議 || response.data || '無法取得建議'
        const md = new MarkdownIt({ html: false, breaks: true, linkify: true })
        const adviceHtmlText = md.render(adviceText)
        userStore.setAdvice(adviceText, adviceHtmlText)
      } catch (e) {
        userStore.setAdvice('取得建議失敗，請稍後再試', '<p>取得建議失敗，請稍後再試</p>')
      } finally {
        adviceLoading.value = false
      }
    }

    function logout() {
      userStore.logout()
      isLoggedIn.value = false
    }

    return {
      isLoggedIn,
      todayCalories,
      targetCalories,
      advice,
      adviceHtml,
      adviceLoading,
      getAdvice,
      logout
    }
  }
}
</script>

<style scoped>
.home {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.dashboard {
  display: flex;
  gap: 20px;
  margin-top: 20px;
}

.card {
  flex: 1;
  padding: 20px;
  background-color: #f5f5f5;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

h1 {
  color: #2c3e50;
  margin-bottom: 30px;
}

h2 {
  color: #42b983;
}

h3 {
  color: #2c3e50;
  margin-bottom: 10px;
}

p {
  font-size: 24px;
  color: #42b983;
  font-weight: bold;
}

.advice-btn {
  margin-top: 30px;
  padding: 12px 24px;
  background-color: #2980b9;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 18px;
  cursor: pointer;
  transition: background 0.2s;
}
.advice-btn:disabled {
  background: #a8d5c2;
  cursor: not-allowed;
}
.advice-btn:hover:not(:disabled) {
  background: #1c5980;
}
.advice-box {
  margin-top: 24px;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 24px 24px 18px 24px;
  box-shadow: 0 2px 8px #a084e820;
  color: #333;
  font-size: 1.1em;
  line-height: 1.8;
}
.advice-box h1,
.advice-box h2,
.advice-box h3 {
  color: #2980b9;
  margin-top: 18px;
  margin-bottom: 10px;
  font-weight: bold;
}
.advice-box ul {
  margin: 10px 0 10px 24px;
  padding-left: 0;
}
.advice-box li {
  margin-bottom: 8px;
  font-size: 1.05em;
}
.advice-box strong {
  color: #42b983;
}
.advice-box em {
  color: #e67e22;
}
</style> 