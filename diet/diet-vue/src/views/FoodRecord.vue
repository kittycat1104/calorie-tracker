<template>
  <div class="food-record">
    <h2>記錄食物攝取</h2>
    <div v-if="isLoggedIn" class="record-container">
      <div class="search-section">
        <div class="form-group">
          <label for="search">搜尋食物：</label>
          <input 
            type="text" 
            id="search" 
            v-model="searchKeyword"
            @input="handleSearch"
            placeholder="請輸入食物名稱"
          >
        </div>
        
        <div v-if="isSearching" class="loading">
          搜尋中...
        </div>
        
        <div v-else-if="searchResults.length > 0" class="search-results">
          <div 
            v-for="food in searchResults" 
            :key="food.foodId"
            class="food-item"
            @click="selectFood(food)"
          >
            <span class="food-name">{{ food.name }}</span>
            <span class="food-calories">{{ food.calories }} 卡路里</span>
            <span v-if="food.suggestedCalories" class="suggested-calories">
              建議: {{ food.suggestedCalories }} 卡路里
            </span>
          </div>
        </div>
        
        <div v-else-if="searchKeyword && !isSearching" class="no-results">
          找不到相關食物
        </div>
      </div>

      <form @submit.prevent="handleSubmit" class="record-form">
        <div class="form-group">
          <label for="food">食物名稱：</label>
          <div class="input-with-button">
            <input 
              type="text" 
              id="food" 
              v-model="foodName" 
              required
              placeholder="請輸入食物名稱"
            >
            <button type="button" class="suggest-btn" @click="getSuggestion" :disabled="!foodName">
              建議熱量
            </button>
          </div>
        </div>
        <div v-if="suggestion" class="suggestion-box">
          {{ suggestion }}
        </div>
        <div class="form-group">
          <label for="calories">卡路里：</label>
          <input 
            type="number" 
            id="calories" 
            v-model="calories" 
            required
            min="0"
            placeholder="請輸入卡路里"
          >
        </div>
        <button type="submit" :disabled="isSubmitting">
          {{ isSubmitting ? '送出中...' : '記錄' }}
        </button>
      </form>
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
import debounce from 'lodash/debounce'

export default {
  name: 'FoodRecordView',
  data() {
    return {
      foodName: '',
      calories: '',
      isSubmitting: false,
      searchKeyword: '',
      searchResults: [],
      isSearching: false,
      suggestion: ''
    }
  },
  computed: {
    ...mapState({
      isLoggedIn: state => !!state.token,
      userId: state => state.user?.id
    })
  },
  created() {
    // 使用 debounce 來限制搜尋請求的頻率
    this.debouncedSearch = debounce(this.searchFoods, 300)
  },
  methods: {
    async handleSearch() {
      if (this.searchKeyword.trim()) {
        this.isSearching = true
        await this.debouncedSearch()
      } else {
        this.searchResults = []
      }
    },
    async searchFoods() {
      try {
        const response = await axios.get(
          `http://localhost:8081/api/foods/search`,
          {
            params: {
              keyword: this.searchKeyword
            }
          }
        )
        // 為每個食物獲取建議熱量
        const foodsWithSuggestions = await Promise.all(
          response.data.map(async (food) => {
            try {
              const suggestionResponse = await axios.get(
                `http://localhost:8081/api/nutrition/check`,
                {
                  params: {
                    food: food.name
                  }
                }
              )
              return {
                ...food,
                suggestedCalories: suggestionResponse.data.calories
              }
            } catch (error) {
              console.error('獲取建議熱量失敗：', error)
              return food
            }
          })
        )
        this.searchResults = foodsWithSuggestions
      } catch (error) {
        console.error('搜尋失敗：', error)
        this.searchResults = []
      } finally {
        this.isSearching = false
      }
    },
    selectFood(food) {
      this.foodName = food.name
      this.calories = food.suggestedCalories || food.calories
      this.searchResults = []
      this.searchKeyword = ''
    },
    async handleSubmit() {
      if (!this.isLoggedIn) {
        alert('請先登入')
        return
      }

      this.isSubmitting = true
      try {
        await axios.post(
          `http://localhost:8081/api/foods`,
          {
            name: this.foodName,
            calories: parseInt(this.calories)
          },
          {
            headers: {
              'Authorization': `Bearer ${this.$store.state.token}`
            }
          }
        )
        
        alert('記錄成功！')
        this.foodName = ''
        this.calories = ''
      } catch (error) {
        console.error('記錄失敗：', error)
        alert('記錄失敗，或重複紀錄!')
      } finally {
        this.isSubmitting = false
      }
    },
    async getSuggestion() {
      if (!this.foodName) return;
      try {
        const response = await axios.get(
          `http://localhost:8081/api/nutrition/check`,
          {
            params: {
              food: this.foodName
            }
          }
        );
        this.suggestion = response.data.意見;
      } catch (error) {
        console.error('獲取建議失敗：', error);
        this.suggestion = '無法獲取建議熱量';
      }
    }
  }
}
</script>

<style scoped>
.food-record {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.record-container {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.search-section {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
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

.search-results {
  margin-top: 10px;
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: white;
}

.food-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  cursor: pointer;
  border-bottom: 1px solid #eee;
}

.food-item:last-child {
  border-bottom: none;
}

.food-item:hover {
  background-color: #f8f9fa;
}

.food-name {
  font-weight: 500;
  color: #2c3e50;
}

.food-calories {
  color: #42b983;
  font-weight: bold;
}

.record-form {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
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

.loading {
  text-align: center;
  padding: 10px;
  color: #666;
}

.no-results {
  text-align: center;
  padding: 10px;
  color: #666;
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

.suggested-calories {
  color: #666;
  font-size: 0.9em;
  margin-left: 10px;
}

.input-with-button {
  display: flex;
  gap: 10px;
}

.input-with-button input {
  flex: 4;
}

.suggest-btn {
  flex: 1;
  padding: 10px 5px;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  white-space: nowrap;
  transition: background-color 0.3s;
  font-size: 0.9rem;
}

.suggest-btn:hover:not(:disabled) {
  background-color: #3aa876;
}

.suggest-btn:disabled {
  background-color: #a8d5c2;
  cursor: not-allowed;
}

.suggestion-box {
  margin: 10px 0;
  padding: 10px;
  background-color: #f8f9fa;
  border-radius: 4px;
  color: #666;
  font-style: italic;
}
</style> 