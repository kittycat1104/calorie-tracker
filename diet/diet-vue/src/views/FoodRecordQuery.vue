<template>
  <div class="food-record-query">
    <div v-if="isLoggedIn" class="query-container">
      <div class="date-picker">
        <label for="date">選擇日期：</label>
        <input 
          type="date" 
          id="date" 
          v-model="selectedDate"
          :max="today"
          @change="fetchFoodRecord"
        >
      </div>

      <!-- 一週熱量攝取折線圖 -->
      <div v-if="weeklyCalories.length" class="weekly-chart">
        <canvas id="weeklyCaloriesChart"></canvas>
      </div>

      <div v-if="loading" class="loading">
        載入中...
      </div>

      <div v-else-if="error" class="error">
        {{ error }}
      </div>

      <div v-else-if="foodRecord" class="record-summary">
        <div class="summary-card">
          <h3>總攝取熱量</h3>
          <p class="calories">{{ foodRecord.totalCalories }} 卡路里</p>
        </div>
        <div class="summary-card">
          <h3>目標熱量</h3>
          <p class="calories">{{ foodRecord.goalCalories }} 卡路里</p>
        </div>
        <div class="summary-card">
          <h3>剩餘熱量</h3>
          <p class="calories" :class="{ 'warning': foodRecord.remainingCalories < 0 }">
            {{ foodRecord.remainingCalories }} 卡路里
          </p>
        </div>
      </div>

      <div v-else class="no-data">
        沒有找到該日期的記錄
      </div>
    </div>
    <div v-else class="login-required">
      <p>請先登入以使用此功能</p>
      <router-link to="/login" class="login-link">前往登入</router-link>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { mapState } from 'vuex'
import Chart from 'chart.js/auto'
import { nextTick } from 'vue'

export default {
  name: 'FoodRecordQueryView',
  data() {
    return {
      selectedDate: new Date().toISOString().split('T')[0],
      foodRecord: null,
      loading: false,
      error: null,
      weeklyCalories: []
    }
  },
  computed: {
    ...mapState({
      isLoggedIn: state => !!state.token,
      userId: state => state.user?.id
    }),
    today() {
      return new Date().toISOString().split('T')[0]
    }
  },
  methods: {
    async fetchFoodRecord() {
      if (!this.isLoggedIn) return

      this.loading = true
      this.error = null
      this.foodRecord = null

      try {
        const response = await axios.get(
          `http://localhost:8081/api/food-record/${this.userId}/date/${this.selectedDate}`,
          {
            headers: {
              'Authorization': `Bearer ${this.$store.state.token}`
            }
          }
        )
        this.foodRecord = response.data
        await this.fetchWeeklyCalories(this.selectedDate)
      } catch (error) {
        console.error('查詢失敗：', error)
        this.error = '查詢失敗，請稍後再試'
      } finally {
        this.loading = false
      }
    },
    async fetchWeeklyCalories(date) {
      try {
        let url = 'http://localhost:8081/api/foods/weekly-calories';
        if (date) {
          url += `?date=${date}`;
        }
        const response = await axios.get(url, {
          headers: {
            'Authorization': `Bearer ${this.$store.state.token}`
          }
        })
        this.weeklyCalories = response.data
        await nextTick()
        this.renderWeeklyChart()
      } catch (error) {
        this.weeklyCalories = []
      }
    },
    renderWeeklyChart() {
      if (!this.weeklyCalories.length) return
      if (this._weeklyChart) {
        this._weeklyChart.destroy()
      }
      const ctx = document.getElementById('weeklyCaloriesChart').getContext('2d')
      this._weeklyChart = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: this.weeklyCalories.map(item => item.date.slice(5)),
          datasets: [{
            label: '每日總攝取熱量',
            data: this.weeklyCalories.map(item => item.totalCalories),
            backgroundColor: '#42b983',
            borderRadius: 8,
            maxBarThickness: 40
          }]
        },
        options: {
          responsive: true,
          plugins: {
            legend: {
              display: false
            },
            tooltip: {
              callbacks: {
                label: ctx => `${ctx.parsed.y} 卡路里`
              }
            }
          },
          scales: {
            y: {
              beginAtZero: true,
              title: {
                display: true,
                text: '卡路里'
              }
            },
            x: {
              title: {
                display: true,
                text: '日期'
              }
            }
          }
        }
      })
    }
  },
  created() {
    if (this.isLoggedIn) {
      this.fetchFoodRecord()
    }
  },
  watch: {
    isLoggedIn(val) {
      if (val) {
        this.fetchFoodRecord()
      }
    }
  }
}
</script>

<style scoped>
.food-record-query {
  max-width: 800px;
  margin: 0 auto;
  padding: 0px;
}

.query-container {
  margin-top: 20px;
}

.date-picker {
  margin-bottom: 30px;
}

.date-picker label {
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #2c3e50;
}

.date-picker input {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
  width: 200px;
}

.weekly-chart {
  margin-bottom: 30px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px #a084e820;
  padding: 20px 10px 10px 10px;
}

#weeklyCaloriesChart {
  max-width: 100%;
  height: 300px;
}

.record-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.summary-card {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.summary-card h3 {
  color: #2c3e50;
  margin-bottom: 10px;
  font-size: 1.1em;
}

.calories {
  font-size: 24px;
  font-weight: bold;
  color: #42b983;
  margin: 0;
}

.calories.warning {
  color: #dc3545;
}

.loading {
  text-align: center;
  padding: 20px;
  color: #666;
}

.error {
  text-align: center;
  padding: 20px;
  color: #dc3545;
  background-color: #f8d7da;
  border-radius: 4px;
}

.no-data {
  text-align: center;
  padding: 20px;
  color: #666;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.login-required {
  text-align: center;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.login-link {
  display: inline-block;
  margin-top: 10px;
  color: #42b983;
  text-decoration: none;
  font-weight: bold;
}

.login-link:hover {
  text-decoration: underline;
}
</style> 