import React, { useState, useEffect } from 'react';
import { userService } from '../services/api';

function Profile() {
  const [profile, setProfile] = useState({
    fullName: '',
    age: '',
    weight: '',
    height: '',
    gender: '',
    activityLevel: '',
    dailyCalorieGoal: ''
  });
  const [recommendedCalories, setRecommendedCalories] = useState(null);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      const response = await userService.getProfile();
      setProfile({
        fullName: response.data.fullName || '',
        age: response.data.age || '',
        weight: response.data.weight || '',
        height: response.data.height || '',
        gender: response.data.gender || '',
        activityLevel: response.data.activityLevel || '',
        dailyCalorieGoal: response.data.dailyCalorieGoal || ''
      });
      setLoading(false);
    } catch (error) {
      console.error('Failed to load profile:', error);
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    setProfile({
      ...profile,
      [e.target.name]: e.target.value
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setMessage('');

    try {
      await userService.updateProfile(profile);
      setMessage('Profile updated successfully!');
    } catch (error) {
      setMessage('Failed to update profile');
    }
  };

  const calculateRecommended = async () => {
    try {
      const response = await userService.getRecommendedCalories();
      setRecommendedCalories(response.data);
    } catch (error) {
      console.error('Failed to calculate recommended calories:', error);
    }
  };

  if (loading) {
    return <div className="container"><p>Loading...</p></div>;
  }

  return (
    <div className="container">
      <h1>Profile</h1>

      <div className="card">
        <form onSubmit={handleSubmit}>
          <div>
            <label>Full Name:</label>
            <input
              type="text"
              name="fullName"
              value={profile.fullName}
              onChange={handleChange}
            />
          </div>

          <div>
            <label>Age:</label>
            <input
              type="number"
              name="age"
              value={profile.age}
              onChange={handleChange}
            />
          </div>

          <div>
            <label>Weight (kg):</label>
            <input
              type="number"
              step="0.1"
              name="weight"
              value={profile.weight}
              onChange={handleChange}
            />
          </div>

          <div>
            <label>Height (cm):</label>
            <input
              type="number"
              step="0.1"
              name="height"
              value={profile.height}
              onChange={handleChange}
            />
          </div>

          <div>
            <label>Gender:</label>
            <select name="gender" value={profile.gender} onChange={handleChange}>
              <option value="">Select Gender</option>
              <option value="MALE">Male</option>
              <option value="FEMALE">Female</option>
              <option value="OTHER">Other</option>
            </select>
          </div>

          <div>
            <label>Activity Level:</label>
            <select name="activityLevel" value={profile.activityLevel} onChange={handleChange}>
              <option value="">Select Activity Level</option>
              <option value="SEDENTARY">Sedentary</option>
              <option value="LIGHTLY_ACTIVE">Lightly Active</option>
              <option value="MODERATELY_ACTIVE">Moderately Active</option>
              <option value="VERY_ACTIVE">Very Active</option>
              <option value="EXTREMELY_ACTIVE">Extremely Active</option>
            </select>
          </div>

          <div>
            <label>Daily Calorie Goal:</label>
            <input
              type="number"
              name="dailyCalorieGoal"
              value={profile.dailyCalorieGoal}
              onChange={handleChange}
            />
          </div>

          {message && <div className={message.includes('success') ? 'success' : 'error'}>{message}</div>}

          <button type="submit">Update Profile</button>
          <button type="button" onClick={calculateRecommended} style={{ marginLeft: '10px', backgroundColor: '#2196F3' }}>
            Calculate Recommended Calories
          </button>
        </form>

        {recommendedCalories && (
          <div style={{ marginTop: '20px', padding: '15px', backgroundColor: '#e7f3ff', borderRadius: '4px' }}>
            <strong>Recommended Daily Calories: {recommendedCalories} kcal</strong>
          </div>
        )}
      </div>
    </div>
  );
}

export default Profile;
