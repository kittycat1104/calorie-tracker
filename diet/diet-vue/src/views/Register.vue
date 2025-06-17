<template>
  <div class="register">
    <h2>註冊</h2>
    <form @submit.prevent="handleRegister">
      <div class="form-group">
        <label for="name">姓名：</label>
        <input type="text" id="username" v-model="username" required>
      </div>
      <!-- <div class="form-group">
        <label for="email">電子郵件：</label>
        <input type="email" id="email" v-model="email" required>
      </div> -->
      <div class="form-group">
        <label for="password">密碼：</label>
        <input type="password" id="password" v-model="password" required>
      </div>
      <div class="form-group">
        <label for="confirmPassword">確認密碼：</label>
        <input type="password" id="confirmPassword" v-model="confirmPassword" required>
      </div>
      <button type="submit">註冊</button>
    </form>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'RegisterView',
  data() {
    return {
      username: '',
      // email: '',
      password: '',
      confirmPassword: ''
    }
  },
  methods: {
    async handleRegister() {
      if (this.password !== this.confirmPassword) {
        alert('密碼不一致')
        return
      }

      try {
        const response = await axios.post('http://localhost:8081/api/auth/register', {
          username: this.username,
          password: this.password
        })
        console.log('註冊成功:', response.data) 
        alert('註冊成功！')
        this.$router.push('/login')
      } catch (error) {
        console.error('註冊失敗：', error)
        alert('註冊失敗，請稍後再試')
      }
    }
  }
}
</script>

<style scoped>
.register {
  max-width: 400px;
  margin: 0 auto;
  padding: 20px;
}

.form-group {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 5px;
}

input {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

button {
  width: 100%;
  padding: 10px;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: #3aa876;
}
</style> 