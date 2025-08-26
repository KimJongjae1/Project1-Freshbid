import { create } from "zustand";
import { persist } from "zustand/middleware";

interface UserResponse {
	role: string;
	username: string;
	nickname: string;
	profileImage: string | null;
}

// Define what part of the state should be persisted
type PersistedUserState = {
  accessToken: string | null
  role: string | null
  username: string | null
  nickname: string | null
  profileImage: string | null
  isLoggedIn: boolean
}

// Full state includes methods
interface UserState extends PersistedUserState {
  login: (token: string) => void
  updateUser: (name: string) => void
  logout: () => void
	setUserInfo: (response: UserResponse) => void;
}

export const useUserStore = create<UserState>()(
  persist(
    (set) => ({
      accessToken: null,
      role: null,
      username: null,
      nickname: null,
      profileImage: null,
      isLoggedIn: false,
      login: (token) => {
        set({
          accessToken: token,
        //   role: res.role,
        //   username: res.username,
        //   nickname: res.nickname,
        //   profileImage: res.profileImage,
          isLoggedIn: true,
        })
      },
      updateUser: (name: string) => {
        set({
          username: name,
        })
      },
      logout: () => {
        set({
          accessToken: null,
          role: null,
          username: null,
          nickname: null,
          profileImage: null,
          isLoggedIn: false,
        })
      },
	  	setUserInfo: (response: UserResponse) => {
        set({
          username: response.username,
          nickname: response.nickname,
          profileImage: response.profileImage,
          role: response.role
        })
      },
		
    }),
    {
      name: 'user-storage',
      partialize: (state) => ({
        accessToken: state.accessToken,
        role: state.role,
        username: state.username,
        nickname: state.nickname,
        profileImage: state.profileImage,
        isLoggedIn: state.isLoggedIn,
      }),
    }
  )
)
