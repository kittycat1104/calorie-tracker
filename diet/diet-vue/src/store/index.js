import { createStore } from 'vuex'
import axios from 'axios'

export default createStore({
  state: {
    user: null,
    token: localStorage.getItem('token') || null
  },
  getters: {
    isAuthenticated: state => !!state.token,
    currentUser: state => state.user
  },
  mutations: {
    setToken(state, token) {
      state.token = token
      localStorage.setItem('token', token)
    },
    setUser(state, user) {
      state.user = user
    },
    logout(state) {
      state.token = null
      state.user = null
      localStorage.removeItem('token')
    }
  },
  actions: {
    async login({ commit }, credentials) {
      try {
        const response = await axios.post('http://localhost:8081/api/auth/login', credentials)
        const { token, user } = response.data
        commit('setToken', token)
        commit('setUser', user)
        return response.data
      } catch (error) {
        console.error('登入失敗：', error)
        throw error
      }
    },
    async register(userData) {
      try {
        const response = await axios.post('http://localhost:8081/api/auth/register', userData)
        return response.data
      } catch (error) {
        console.error('註冊失敗：', error)
        throw error
      }
    },
    async validateToken({ commit, state }) {
      if (!state.token) return false

      try {
        const response = await axios.get('http://localhost:8081/api/auth/check', {
          headers: {
            'Authorization': `Bearer ${state.token}`
          }
        })
        if (response.data.user) {
          commit('setUser', response.data.user)
          return true
        } else {
          commit('logout')
          return false
        }
      } catch (error) {
        commit('logout')
        return false
      }
    },
    logout({ commit }) {
      commit('logout')
    }
  }
}) 