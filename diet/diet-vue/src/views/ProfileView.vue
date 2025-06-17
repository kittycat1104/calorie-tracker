<template>
  <div class="profile-container">
    <div class="profile-card">
      <h2 class="profile-title">個人資料</h2>
      
      <div class="profile-section">
        <h3>基本資訊</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">用戶名</span>
            <span class="value">{{ userProfile.username }}</span>
          </div>
          <div class="info-item">
            <span class="label">身高</span>
            <span class="value">{{ userProfile.height }} cm</span>
          </div>
          <div class="info-item">
            <span class="label">體重</span>
            <span class="value">{{ userProfile.weight }} kg</span>
          </div>
          <div class="info-item">
            <span class="label">年齡</span>
            <span class="value">{{ userProfile.age }} 歲</span>
          </div>
          <div class="info-item">
            <span class="label">性別</span>
            <span class="value">{{ userProfile.gender === 'female' ? '女性' : '男性' }}</span>
          </div>
          <div class="info-item">
            <span class="label">活動係數</span>
            <span class="value">{{ userProfile.activityLevel }}</span>
          </div>
        </div>
      </div>

      <div class="profile-section">
        <h3>健康目標</h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">每週目標減重</span>
            <span class="value">{{ userProfile.weeklyWeightGoal }} kg</span>
          </div>
          <div class="info-item">
            <span class="label">基礎代謝率 (TDEE)</span>
            <span class="value">{{ userProfile.tdee }} 卡路里</span>
          </div>
          <div class="info-item">
            <span class="label">每日熱量赤字</span>
            <span class="value">{{ userProfile.calorieDeficit }} 卡路里</span>
          </div>
          <div class="info-item">
            <span class="label">每日目標熱量</span>
            <span class="value highlight">{{ userProfile.dailyCalorieGoal }} 卡路里</span>
          </div>
        </div>
      </div>

      <div class="profile-actions">
        <button class="edit-btn" @click="openEditModal">編輯資料</button>
      </div>
    </div>

    <!-- 編輯表單彈窗 -->
    <div v-if="showEditModal" class="modal-overlay">
      <div class="modal-content">
        <h3>編輯個人資料</h3>
        <form @submit.prevent="submitEdit">
          <div class="form-group">
            <label>身高 (cm)</label>
            <input type="number" v-model.number="editForm.height" min="100" max="250" step="0.1" required />
          </div>
          <div class="form-group">
            <label>體重 (kg)</label>
            <input type="number" v-model.number="editForm.weight" min="30" max="200" step="0.1" required />
          </div>
          <div class="form-group">
            <label>年齡</label>
            <input type="number" v-model.number="editForm.age" min="15" max="100" required />
          </div>
          <div class="form-group">
            <label>性別</label>
            <select v-model="editForm.gender" required>
              <option value="female">女性</option>
              <option value="male">男性</option>
            </select>
          </div>
          <div class="form-group">
            <label>活動係數</label>
            <select v-model.number="editForm.activityLevel" required>
              <option value="1.2">久坐不動 (1.2)</option>
              <option value="1.375">輕度活動 (1.375)</option>
              <option value="1.55">中度活動 (1.55)</option>
              <option value="1.725">重度活動 (1.725)</option>
              <option value="1.9">極度活動 (1.9)</option>
            </select>
          </div>
          <div class="form-group">
            <label>每週目標減重 (kg)</label>
            <input type="number" v-model.number="editForm.weeklyWeightGoal" min="0" max="1" step="0.1" required />
          </div>
          <div class="modal-actions">
            <button type="submit" :disabled="editLoading">{{ editLoading ? '儲存中...' : '儲存' }}</button>
            <button type="button" @click="closeEditModal" :disabled="editLoading">取消</button>
          </div>
          <div v-if="editError" class="edit-error">{{ editError }}</div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import { mapState } from 'vuex';

export default {
  name: 'ProfileView',
  data() {
    return {
      userProfile: {
        username: '',
        height: 0,
        weight: 0,
        age: 0,
        activityLevel: 0,
        gender: '',
        goal: '',
        tdee: 0,
        weeklyWeightGoal: 0,
        calorieDeficit: 0,
        dailyCalorieGoal: 0
      },
      showEditModal: false,
      editForm: {
        height: 0,
        weight: 0,
        age: 0,
        activityLevel: 0,
        gender: '',
        weeklyWeightGoal: 0
      },
      editLoading: false,
      editError: ''
    };
  },
  computed: {
    ...mapState({
      userId: state => state.user?.id
    })
  },
  async created() {
    await this.fetchUserProfile();
  },
  methods: {
    async fetchUserProfile() {
      try {
        const response = await axios.get(`http://localhost:8081/api/user-profile/user/${this.userId}`, {
          headers: {
            'Authorization': `Bearer ${this.$store.state.token}`
          }
        });
        this.userProfile = response.data;
      } catch (error) {
        console.error('獲取用戶資料失敗：', error);
      }
    },
    openEditModal() {
      this.editForm = {
        height: this.userProfile.height,
        weight: this.userProfile.weight,
        age: this.userProfile.age,
        activityLevel: this.userProfile.activityLevel,
        gender: this.userProfile.gender,
        weeklyWeightGoal: this.userProfile.weeklyWeightGoal
      };
      this.showEditModal = true;
      this.editError = '';
    },
    closeEditModal() {
      this.showEditModal = false;
      this.editError = '';
    },
    async submitEdit() {
      this.editLoading = true;
      this.editError = '';
      try {
        await axios.post(`http://localhost:8081/api/user-profile/${this.userId}`, this.editForm, {
          headers: {
            'Authorization': `Bearer ${this.$store.state.token}`
          }
        });
        await this.fetchUserProfile();
        this.showEditModal = false;
      } catch (error) {
        console.error('更新失敗：', error);
        this.editError = '更新失敗，請稍後再試';
      } finally {
        this.editLoading = false;
      }
    }
  }
};
</script>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 40px auto;
  padding: 0 20px;
}

.profile-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 24px #a084e830;
  padding: 32px;
}

.profile-title {
  color: #a084e8;
  font-size: 1.8em;
  margin-bottom: 24px;
  text-align: center;
}

.profile-section {
  margin-bottom: 32px;
}

.profile-section h3 {
  color: #34495e;
  font-size: 1.3em;
  margin-bottom: 16px;
  padding-bottom: 8px;
  border-bottom: 2px solid #f0f0f0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.info-item {
  background: #f9f9fb;
  padding: 16px;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.label {
  color: #888;
  font-size: 0.9em;
}

.value {
  color: #34495e;
  font-size: 1.2em;
  font-weight: 600;
}

.value.highlight {
  color: #27ae60;
  font-size: 1.4em;
}

.profile-actions {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

.edit-btn {
  background-color: #a084e8;
  color: white;
  border: none;
  padding: 12px 24px;
  font-size: 1.1em;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.edit-btn:hover {
  background-color: #8a6fd1;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 16px;
  padding: 32px;
  width: 400px;
  max-width: 90vw;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content h3 {
  color: #34495e;
  font-size: 1.4em;
  margin-bottom: 24px;
  text-align: center;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-weight: 500;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1em;
  transition: border-color 0.3s ease;
}

.form-group input:focus,
.form-group select:focus {
  outline: none;
  border-color: #a084e8;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.modal-actions button {
  padding: 10px 20px;
  border-radius: 8px;
  border: none;
  font-size: 1em;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.modal-actions button[type="submit"] {
  background-color: #27ae60;
  color: white;
}

.modal-actions button[type="submit"]:disabled {
  background-color: #95a5a6;
  cursor: not-allowed;
}

.modal-actions button[type="button"] {
  background-color: #e74c3c;
  color: white;
}

.modal-actions button[type="button"]:disabled {
  background-color: #95a5a6;
  cursor: not-allowed;
}

.edit-error {
  color: #e74c3c;
  text-align: center;
  margin-top: 16px;
  font-size: 0.9em;
}

@media (max-width: 600px) {
  .profile-container {
    margin: 20px auto;
  }
  
  .profile-card {
    padding: 20px;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .modal-content {
    padding: 20px;
  }
}
</style> 