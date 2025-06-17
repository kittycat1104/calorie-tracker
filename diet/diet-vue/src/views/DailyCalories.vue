<template>
  <div class="daily-calories">
    <h2>每日卡路里記錄</h2>
    <div class="date-picker">
      <label for="date">選擇日期：</label>
      <input type="date" id="date" v-model="selectedDate" @change="fetchCaloriesData">
    </div>
    
    <div class="calories-info" v-if="caloriesData">
      <div class="calories-card">
        <h3>總攝取卡路里</h3>
        <p>{{ caloriesData.totalCalories }} kcal</p>
      </div>
      <div class="calories-card">
        <h3>目標卡路里</h3>
        <p>{{ caloriesData.goalCalories }} kcal</p>
      </div>
      <div class="calories-card">
        <h3>剩餘卡路里</h3>
        <p>{{ caloriesData.remainingCalories }} kcal</p>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'DailyCaloriesView',
  data() {
    return {
      selectedDate: new Date().toISOString().split('T')[0],
      caloriesData: null,
      userId: 1 // 這裡需要根據實際情況設置用戶ID
    }
  },
  methods: {
    async fetchCaloriesData() {
      try {
        const response = await axios.get(`http://localhost:8081/api/food-record/${this.userId}/date/${this.selectedDate}`)
        this.caloriesData = response.data
      } catch (error) {
        console.error('獲取卡路里數據失敗：', error)
        alert('獲取數據失敗，請稍後再試')
      }
    }
  },
  mounted() {
    this.fetchCaloriesData()
  }
}
</script>

<style scoped>
.daily-calories {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.date-picker {
  margin-bottom: 20px;
}

.date-picker label {
  margin-right: 10px;
}

.date-picker input {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.calories-info {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  margin-top: 20px;
}

.calories-card {
  background-color: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.calories-card h3 {
  margin: 0 0 10px 0;
  color: #2c3e50;
}

.calories-card p {
  font-size: 24px;
  font-weight: bold;
  color: #42b983;
  margin: 0;
}
</style> 