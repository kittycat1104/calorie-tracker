import { defineStore } from 'pinia'
// import { ref } from 'vue'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: null,
    advice: '',
    adviceHtml: ''
  }),
  actions: {
    setToken(newToken) {
      this.token = newToken
      if (newToken) {
        localStorage.setItem('token', newToken)
      } else {
        localStorage.removeItem('token')
      }
    },
    setUser(newUser) {
      this.user = newUser
    },
    logout() {
      this.token = ''
      this.user = null
      this.advice = ''
      this.adviceHtml = ''
      localStorage.removeItem('token')
    },
    setAdvice(newAdvice, newAdviceHtml) {
      this.advice = newAdvice
      this.adviceHtml = newAdviceHtml
    },
    clearAdvice() {
      this.advice = ''
      this.adviceHtml = ''
    }
  }
}) 