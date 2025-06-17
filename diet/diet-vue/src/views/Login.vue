<template>
  <div class="login">
    <h2>登入</h2>
    <form @submit.prevent="handleLogin">
      <div class="form-group">
        <label for="username">使用者名稱：</label>
        <input 
          type="text" 
          id="username" 
          v-model="username" 
          required
          placeholder="請輸入使用者名稱"
        >
      </div>
      <div class="form-group">
        <label for="password">密碼：</label>
        <input 
          type="password" 
          id="password" 
          v-model="password" 
          required
          placeholder="請輸入密碼"
        >
      </div>
      <button type="submit" :disabled="isSubmitting">
        {{ isSubmitting ? '登入中...' : '登入' }}
      </button>
    </form>
  </div>
</template>

<script>
import { mapActions } from 'vuex'

export default {
  name: 'LoginView',
  data() {
    return {
      username: '',
      password: '',
      isSubmitting: false
    }
  },
  methods: {
    ...mapActions(['login']),
    async handleLogin() {
      this.isSubmitting = true
      try {
        await this.login({
          username: this.username,
          password: this.password
        })
        
        // 如果有重定向參數，則導向該頁面
        const redirectPath = this.$route.query.redirect || '/'
        this.$router.push(redirectPath)
      } catch (error) {
        console.error('登入失敗：', error)
        alert('登入失敗，請檢查您的帳號密碼')
      } finally {
        this.isSubmitting = false
      }
    }
  }
}
</script>

<style scoped>
.login {
  max-width: 400px;
  margin: 0 auto;
  padding: 20px;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #2c3e50;
}

input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

input:focus {
  outline: none;
  border-color: #42b983;
  box-shadow: 0 0 0 2px rgba(66, 185, 131, 0.2);
}

button {
  width: 100%;
  padding: 12px;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

button:hover:not(:disabled) {
  background-color: #3aa876;
}

button:disabled {
  background-color: #a8d5c2;
  cursor: not-allowed;
}
</style> 