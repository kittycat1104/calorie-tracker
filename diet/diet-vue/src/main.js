import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import { createPinia } from 'pinia'

import '@vuepic/vue-datepicker/dist/main.css'

 
const app = createApp(App)
const pinia = createPinia()
app.use(router)
app.use(store)
app.use(pinia)
app.mount('#app') 