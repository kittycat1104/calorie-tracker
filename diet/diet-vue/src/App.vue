<template>
  <div id="app">
    <nav>
      <router-link to="/">首頁</router-link> |
      <router-link to="/food-record">記錄食物</router-link> |
      <router-link to="/food-record-query">查詢記錄</router-link>|
      <router-link to="/calendar">熱量日曆</router-link> |
      <router-link to="/profile" v-if="isLoggedIn">設定 |</router-link> 
      <router-link to="/login" v-if="!isLoggedIn">登入</router-link>
      <a href="#" @click.prevent="handleLogout" v-else>登出</a> |
      <router-link to="/register" v-if="!isLoggedIn">註冊</router-link>
    </nav>
    <router-view/>
  </div>
</template>

<script>
import { mapState, mapActions } from 'vuex'

export default {
  name: 'App',
  computed: {
    ...mapState({
      isLoggedIn: state => !!state.token
    })
  },
  methods: {
    ...mapActions(['validateToken', 'logout']),
    async handleLogout() {
      await this.logout()
      this.$router.push('/login')
    }
  },
  async created() {
    // 檢查 token 是否有效
    if (this.isLoggedIn) {
      const isValid = await this.validateToken()
      if (!isValid) {
        this.$router.push('/login')
      }
    }
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

nav {
  padding: 30px;
}

nav a {
  font-weight: bold;
  color: #2c3e50;
  text-decoration: none;
  margin: 0 10px;
}

nav a.router-link-exact-active {
  color: #42b983;
}
</style> 