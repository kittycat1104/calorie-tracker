<template>
  <div class="evocalendar-root">
    <!-- 左側：年份與月份選單 -->
    <aside class="evocalendar-sidebar">
      <div class="evocalendar-year-bar">
        <button @click="prevYear" class="evocalendar-arrow">&#60;</button>
        <span class="evocalendar-year">{{ currentYear }}</span>
        <button @click="nextYear" class="evocalendar-arrow">&#62;</button>
      </div>
      <ul class="evocalendar-month-list">
        <li v-for="(m, idx) in months" :key="m"
            :class="{ active: idx === currentMonth }"
            @click="selectMonth(idx)">
          {{ m }}
        </li>
      </ul>
    </aside>
    <!-- 中間：月曆本體 -->
    <section class="evocalendar-calendar">
      <v-calendar
        is-expanded
        :attributes="calendarAttrs"
        :from-page="{ month: currentMonth + 1, year: currentYear }"
        :to-page="{ month: currentMonth + 1, year: currentYear }"
        @dayclick="onDayClick"
        locale="zh-tw"
        :style="{ width: '420px', height: '340px', background: 'white', borderRadius: '16px', boxShadow: '0 2px 12px #a084e830', border: '2px solid #a084e8' }"
      />
    </section>
    <!-- 右側：當日詳細紀錄 -->
    <aside class="evocalendar-detail">
      <div class="evocalendar-detail-header">
        <span class="evocalendar-detail-date">{{ detailDateText }}</span>
      </div>
      <div class="evocalendar-detail-cards">
        <div class="evocalendar-detail-card" v-if="selectedRecord">
          <div class="evocalendar-detail-dot" :class="{ warning: selectedRecord.remainingCalories < 0 }"></div>
          <div class="evocalendar-detail-title">總攝取熱量</div>
          <div class="evocalendar-detail-value">{{ selectedRecord.totalCalories }} 卡路里</div>
        </div>
        <div class="evocalendar-detail-card" v-if="selectedRecord">
          <div class="evocalendar-detail-dot yellow"></div>
          <div class="evocalendar-detail-title">目標熱量</div>
          <div class="evocalendar-detail-value">{{ selectedRecord.goalCalories ?? '-' }} 卡路里</div>
        </div>
        <div class="evocalendar-detail-card" v-if="selectedRecord">
          <div class="evocalendar-detail-dot blue"></div>
          <div class="evocalendar-detail-title">剩餘熱量</div>
          <div class="evocalendar-detail-value" :class="{ warning: selectedRecord.remainingCalories < 0 }">{{ selectedRecord.remainingCalories ?? '-' }} 卡路里</div>
        </div>
      </div>
      <div class="evocalendar-detail-list">
        <h4>當日食物紀錄</h4>
        <div v-if="selectedRecord && selectedRecord.records.length === 0" class="no-records">當日沒有食物記錄</div>
        <div v-else-if="selectedRecord" class="record-items">
          <div v-for="record in selectedRecord.records" :key="record.id" class="record-item">
            <span class="food-name">{{ record.foodName }}</span>
            <span class="food-calories">{{ record.calories }} 卡路里</span>
            <button class="edit-btn" @click="openEdit(record)">編輯</button>
            <button class="delete-btn" @click="deleteRecord(record.id)">刪除</button>
          </div>
        </div>
        <button class="add-btn" @click="openAdd">＋ 新增食物</button>
      </div>
    </aside>
    <!-- 編輯彈窗 -->
    <div v-if="showEditModal" class="modal-overlay">
      <div class="modal-content">
        <h3>編輯食物紀錄</h3>
        <form @submit.prevent="submitEdit">
          <div class="form-group">
            <label>食物名稱</label>
            <input v-model="editForm.name" required />
          </div>
          <div class="form-group">
            <label>卡路里</label>
            <input type="number" v-model.number="editForm.calories" min="0" required />
          </div>
          <div class="modal-actions">
            <button type="submit" :disabled="editLoading">{{ editLoading ? '儲存中...' : '儲存' }}</button>
            <button type="button" @click="closeEdit" :disabled="editLoading">取消</button>
          </div>
          <div v-if="editError" class="edit-error">{{ editError }}</div>
        </form>
      </div>
    </div>
    <!-- 新增彈窗（含即時搜尋） -->
    <div v-if="showAddModal" class="modal-overlay">
      <div class="modal-content">
        <h3>新增食物紀錄</h3>
        <form @submit.prevent="submitAdd">
          <div class="form-group">
            <label>搜尋食物</label>
            <input v-model="addSearchKeyword" @input="handleAddSearch" placeholder="請輸入食物名稱" />
          </div>
          <div v-if="addIsSearching" class="loading">搜尋中...</div>
          <div v-else-if="addSearchResults.length > 0" class="search-results">
            <div v-for="food in addSearchResults" :key="food.foodId" class="food-item" @click="selectAddFood(food)">
              <span class="food-name">{{ food.name }}</span>
              <span class="food-calories">{{ food.calories }} 卡路里</span>
              <span v-if="food.suggestedCalories" class="suggested-calories">
                建議: {{ food.suggestedCalories }} 卡路里
              </span>
            </div>
          </div>
          <div v-else-if="addSearchKeyword && !addIsSearching" class="no-results">找不到相關食物</div>
          <div class="form-group">
            <label>食物名稱</label>
            <div class="input-with-button">
              <input v-model="addForm.name" required />
              <button type="button" class="suggest-btn" @click="getAddSuggestion" :disabled="!addForm.name">
                建議熱量
              </button>
            </div>
          </div>
          <div v-if="addSuggestion" class="suggestion-box">
            {{ addSuggestion }}
          </div>
          <div class="form-group">
            <label>卡路里</label>
            <input type="number" v-model.number="addForm.calories" min="0" required />
          </div>
          <div class="modal-actions">
            <button type="submit" :disabled="addLoading">{{ addLoading ? '新增中...' : '新增' }}</button>
            <button type="button" @click="closeAdd" :disabled="addLoading">取消</button>
          </div>
          <div v-if="addError" class="edit-error">{{ addError }}</div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { Calendar } from 'v-calendar';
import 'v-calendar/style.css';
import axios from 'axios';
import { mapState } from 'vuex';
import debounce from 'lodash/debounce';

function formatDateToYMD(date) {
  const d = new Date(date);
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().split('T')[0];
}

const monthNames = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December'
];

export default {
  name: 'CalendarView',
  components: {
    'v-calendar': Calendar
  },
  data() {
    const now = new Date();
    return {
      selectedDate: new Date(),
      selectedRecord: null,
      loading: false,
      error: null,
      calendarAttrs: [],
      showEditModal: false,
      editForm: {
        id: null,
        name: '',
        calories: 0
      },
      editLoading: false,
      editError: '',
      showAddModal: false,
      addForm: {
        name: '',
        calories: 0
      },
      addLoading: false,
      addError: '',
      addSearchKeyword: '',
      addSearchResults: [],
      addIsSearching: false,
      addSuggestion: '',
      months: monthNames,
      currentMonth: now.getMonth(),
      currentYear: now.getFullYear()
    };
  },
  computed: {
    ...mapState({
      isLoggedIn: state => !!state.token,
      userId: state => state.user?.id
    }),
    detailDateText() {
      const d = new Date(this.selectedDate);
      return d.toLocaleDateString('zh-TW', { year: 'numeric', month: 'long', day: 'numeric' });
    }
  },
  created() {
    this.debouncedAddSearch = debounce(this.searchAddFoods, 300);
    if (this.isLoggedIn) {
      this.handleDateSelect(this.selectedDate);
    }
  },
  methods: {
    prevYear() {
      this.currentYear--;
    },
    nextYear() {
      this.currentYear++;
    },
    selectMonth(idx) {
      this.currentMonth = idx;
      // 切換月份時自動選第一天
      const d = new Date(this.currentYear, this.currentMonth, 1);
      this.selectedDate = d;
      this.handleDateSelect(d);
    },
    async onDayClick(day) {
      this.selectedDate = day.date;
      await this.handleDateSelect(day.date);
    },
    async handleDateSelect(date) {
      if (!this.isLoggedIn) return;
      this.loading = true;
      this.error = null;
      this.selectedRecord = null;
      try {
        const formattedDate = formatDateToYMD(date);
        const response = await axios.get(
          `http://localhost:8081/api/food-record/${this.userId}/date/${formattedDate}`,
          {
            headers: {
              'Authorization': `Bearer ${this.$store.state.token}`
            }
          }
        );
        this.selectedRecord = response.data;
      } catch (error) {
        console.error('查詢失敗：', error);
        this.error = '查詢失敗，請稍後再試';
      } finally {
        this.loading = false;
      }
    },
    openEdit(record) {
      this.editForm = {
        id: record.id,
        name: record.foodName,
        calories: record.calories
      };
      this.editError = '';
      this.showEditModal = true;
    },
    closeEdit() {
      this.showEditModal = false;
      this.editForm = { id: null, name: '', calories: 0 };
      this.editError = '';
    },
    async submitEdit() {
      this.editLoading = true;
      this.editError = '';
      try {
        await axios.put(`http://localhost:8081/api/food-record/${this.editForm.id}`, {
          foodName: this.editForm.name,
          calories: this.editForm.calories
        }, {
          headers: {
            'Authorization': `Bearer ${this.$store.state.token}`
          }
        });
        this.showEditModal = false;
        await this.handleDateSelect(this.selectedDate);
      } catch (e) {
        this.editError = '更新失敗，請稍後再試';
      } finally {
        this.editLoading = false;
      }
    },
    openAdd() {
      this.addForm = { name: '', calories: 0 };
      this.addError = '';
      this.addSearchKeyword = '';
      this.addSearchResults = [];
      this.showAddModal = true;
    },
    closeAdd() {
      this.showAddModal = false;
      this.addForm = { name: '', calories: 0 };
      this.addError = '';
      this.addSearchKeyword = '';
      this.addSearchResults = [];
      this.addSuggestion = '';
    },
    async submitAdd() {
      this.addLoading = true;
      this.addError = '';
      try {
        await axios.post(`http://localhost:8081/api/food-record/${this.userId}`, {
          foodName: this.addForm.name,
          calories: this.addForm.calories,
          date: formatDateToYMD(this.selectedDate)
        }, {
          headers: {
            'Authorization': `Bearer ${this.$store.state.token}`
          }
        });
        this.showAddModal = false;
        await this.handleDateSelect(this.selectedDate);
      } catch (e) {
        this.addError = '新增失敗，請稍後再試';
      } finally {
        this.addLoading = false;
      }
    },
    async deleteRecord(id) {
      if (!confirm('確定要刪除這筆紀錄嗎？')) return;
      try {
        await axios.delete(`http://localhost:8081/api/food-record/${id}`, {
          headers: {
            'Authorization': `Bearer ${this.$store.state.token}`
          }
        });
        await this.handleDateSelect(this.selectedDate);
      } catch (e) {
        alert('刪除失敗，請稍後再試');
      }
    },
    async handleAddSearch() {
      if (this.addSearchKeyword.trim()) {
        this.addIsSearching = true;
        await this.debouncedAddSearch();
      } else {
        this.addSearchResults = [];
      }
    },
    async searchAddFoods() {
      try {
        const response = await axios.get(
          `http://localhost:8081/api/foods/search`,
          {
            params: {
              keyword: this.addSearchKeyword
            }
          }
        );
        this.addSearchResults = response.data;
      } catch (error) {
        this.addSearchResults = [];
      } finally {
        this.addIsSearching = false;
      }
    },
    selectAddFood(food) {
      this.addForm.name = food.name;
      this.addForm.calories = food.calories;
      this.addSearchResults = [];
      this.addSearchKeyword = '';
    },
    async getAddSuggestion() {
      if (!this.addForm.name) return;
      try {
        const response = await axios.get(
          `http://localhost:8081/api/nutrition/check`,
          {
            params: {
              food: this.addForm.name
            }
          }
        );
        this.addSuggestion = response.data.意見;
      } catch (error) {
        console.error('獲取建議失敗：', error);
        this.addSuggestion = '無法獲取建議熱量';
      }
    }
  }
};
</script>

<style scoped>
.evocalendar-root {
  display: flex;
  background: #f9f9fb;
  border-radius: 18px;
  box-shadow: 0 4px 24px #a084e830;
  max-width: 1200px;
  margin: 30px auto;
  min-height: 600px;
  overflow: hidden;
}
.evocalendar-sidebar {
  background: #a084e8;
  color: #fff;
  width: 180px;
  padding: 30px 0 0 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 140px;
}
.evocalendar-year-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 18px;
}
.evocalendar-arrow {
  background: none;
  border: none;
  color: #fff;
  font-size: 1.3em;
  font-weight: bold;
  cursor: pointer;
  margin: 0 8px;
  transition: color 0.2s;
}
.evocalendar-arrow:hover {
  color: #ffd166;
}
.evocalendar-year {
  font-size: 1.2em;
  font-weight: bold;
  letter-spacing: 1px;
}
.evocalendar-month-list {
  list-style: none;
  padding: 0;
  margin: 0;
  width: 100%;
}
.evocalendar-month-list li {
  padding: 10px 0 10px 30px;
  cursor: pointer;
  font-size: 1.08em;
  border-left: 4px solid transparent;
  transition: background 0.2s, border-color 0.2s;
}
.evocalendar-month-list li.active,
.evocalendar-month-list li:hover {
  background: #fff2;
  border-left: 4px solid #ffd166;
  color: #ffd166;
}
.evocalendar-calendar {
  flex: 1 1 420px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 0;
}
.evocalendar-detail {
  flex: 1 1 350px;
  background: #fff;
  padding: 32px 28px 32px 24px;
  display: flex;
  flex-direction: column;
  min-width: 320px;
  box-shadow: -2px 0 12px #a084e820;
}
.evocalendar-detail-header {
  margin-bottom: 18px;
}
.evocalendar-detail-date {
  font-size: 1.2em;
  font-weight: bold;
  color: #a084e8;
  letter-spacing: 1px;
}
.evocalendar-detail-cards {
  display: flex;
  gap: 12px;
  margin-bottom: 18px;
}
.evocalendar-detail-card {
  flex: 1 1 0;
  background: #f9f9fb;
  border-radius: 10px;
  box-shadow: 0 1px 6px #a084e820;
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 0;
}
.evocalendar-detail-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-bottom: 6px;
  background: #ff6b81;
}
.evocalendar-detail-dot.yellow {
  background: #ffd166;
}
.evocalendar-detail-dot.blue {
  background: #2980b9;
}
.evocalendar-detail-dot.warning {
  background: #e74c3c;
}
.evocalendar-detail-title {
  font-size: 0.98em;
  color: #888;
  margin-bottom: 2px;
}
.evocalendar-detail-value {
  font-size: 1.15em;
  font-weight: bold;
  color: #34495e;
}
.evocalendar-detail-value.warning {
  color: #e74c3c;
}
.evocalendar-detail-list {
  margin-top: 18px;
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
}
.evocalendar-detail-list h4 {
  font-size: 1.08em;
  font-weight: 600;
  margin-bottom: 10px;
  color: #a084e8;
}
.no-records {
  color: #bbb;
  font-style: italic;
  text-align: center;
  padding: 18px 0;
}
.record-items {
  flex-grow: 1;
  overflow-y: auto;
}
.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #eee;
  padding: 8px 0;
  font-size: 1rem;
  color: #444;
}
.food-name {
  flex: 1 1 40%;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.food-calories {
  flex: 1 1 30%;
  text-align: right;
  color: #27ae60;
  font-weight: 600;
  user-select: none;
}
.edit-btn,
.delete-btn {
  border: none;
  padding: 4px 10px;
  margin-left: 8px;
  font-size: 0.9rem;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.25s ease;
  user-select: none;
}
.edit-btn {
  background-color: #f39c12;
  color: white;
}
.edit-btn:hover {
  background-color: #d78e0e;
}
.delete-btn {
  background-color: #e74c3c;
  color: white;
}
.delete-btn:hover {
  background-color: #c0392b;
}
.add-btn {
  background-color: #2980b9;
  color: white;
  border: none;
  padding: 6px 14px;
  font-size: 1rem;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.25s ease;
  margin-top: 12px;
  width: 100%;
  display: block;
  text-align: center;
}
.add-btn:hover {
  background-color: #1c5980;
}
.loading,
.error,
.edit-error {
  color: #888;
  text-align: center;
  padding: 12px 0;
}
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.modal-content {
  background: white;
  border-radius: 8px;
  padding: 25px 30px;
  width: 400px;
  max-width: 90vw;
  box-shadow: 0 3px 12px rgb(0 0 0 / 0.25);
}
.form-group {
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
}
.form-group label {
  font-weight: 600;
  margin-bottom: 6px;
  color: #333;
}
.form-group input {
  padding: 8px 10px;
  font-size: 1rem;
  border-radius: 5px;
  border: 1px solid #ccc;
  transition: border-color 0.25s ease;
}
.form-group input:focus {
  outline: none;
  border-color: #2980b9;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 15px;
}
.modal-actions button {
  padding: 8px 14px;
  border-radius: 5px;
  border: none;
  font-size: 1rem;
  cursor: pointer;
  user-select: none;
  transition: background-color 0.3s ease;
}
.modal-actions button[type="submit"] {
  background-color: #27ae60;
  color: white;
}
.modal-actions button[type="submit"]:disabled {
  background-color: #88c57d;
  cursor: not-allowed;
}
.modal-actions button[type="button"] {
  background-color: #bbb;
  color: #333;
}
.modal-actions button[type="button"]:disabled {
  background-color: #ddd;
  cursor: not-allowed;
}
.search-results {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #ccc;
  border-radius: 6px;
  margin-bottom: 15px;
  background: #fff;
}
.food-item {
  padding: 10px 14px;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  font-weight: 500;
  color: #34495e;
  border-bottom: 1px solid #eee;
  user-select: none;
}
.food-item:hover {
  background-color: #f0f8ff;
}
.no-results {
  color: #999;
  font-style: italic;
  text-align: center;
  padding: 8px;
}
.input-with-button {
  display: flex;
  gap: 10px;
}
.input-with-button input {
  flex: 1;
}
.suggest-btn {
  padding: 8px 12px;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  white-space: nowrap;
  transition: background-color 0.3s;
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
@media (max-width: 1024px) {
  .evocalendar-root {
    flex-direction: column;
    min-width: 0;
    min-height: 0;
  }
  .evocalendar-sidebar {
    flex-direction: row;
    width: 100%;
    min-width: 0;
    padding: 10px 0;
    justify-content: center;
  }
  .evocalendar-calendar {
    padding: 20px 0;
  }
  .evocalendar-detail {
    min-width: 0;
    padding: 20px 10px;
  }
}
</style>