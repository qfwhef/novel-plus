 <template>
  <div class="topMain">
    <div class="box_center cf">
      <router-link :to="{ name: 'home' }" class="logo fl"
        ><img :src="logo" alt="享受阅读"
      /></router-link>
      <div class="searchBar fl">
        <div class="search cf">
          <input
            v-model="keyword"
            type="text"
            placeholder="书名、作者、关键字"
            class="s_int"
            v-on:keyup.enter="searchByK"
          />
          <label class="search_btn" id="btnSearch" @click="searchByK()"
            ><i class="icon"></i
          ></label>
        </div>
      </div>

      <div class="bookShelf fr" id="headerUserInfo">
        <router-link v-if="token" :to="{ name: 'bookshelf' }" class="sj_link">我的书架</router-link>
        <span v-if="!token" class="user_link"
          ><!--<i class="line mr20">|</i
          >-->
          <router-link :to="{ name: 'login' }" class="mr15">登录</router-link>
          <router-link :to="{ name: 'register' }" class="mr15"
            >注册</router-link
          >
        </span>
        <span v-if="token" class="user_link"
          ><!--<i class="line mr20">|</i
          >--><router-link :to="{name:'userSetup'}" class="mr15">{{ userName }}</router-link>
          <a @click="logout" href="javascript:void(0)">退出</a></span
        >
      </div>
    </div>
  </div>
</template>

<script>
import logo from "@/assets/images/logo.png";
import { reactive, toRefs, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { getToken, getNickName, getUserName, removeToken, removeNickName, removeUserName, removeUid } from "@/utils/auth";
import { logout as logoutApi } from "@/api/user";
export default {
  name: "Top",
  setup(props, context) {
    const state = reactive({
      keyword: "",
      nickName: getNickName(),
      userName: getUserName(),
      token: getToken(),
    });
    state.nickName = getNickName();
    state.userName = getUserName();
    state.token = getToken();
    const route = useRoute();
    const router = useRouter();
    state.keyword = route.query.key;
    const searchByK = () => {
      router.push({ path: "/bookclass", query: { key: state.keyword } });
      context.emit("eventSerch", state.keyword);
    };
    const logout = async () => {
      try {
        // 调用后端登出接口
        await logoutApi();
        
        // 清除本地存储的用户信息
        removeToken();
        removeNickName();
        removeUserName();
        removeUid();
        
        // 更新状态
        state.nickName = "";
        state.userName = "";
        state.token = "";
        
        ElMessage.success("退出成功");
        
        // 跳转到首页
        router.push({ path: "/login" });
      } catch (error) {
        console.error('登出失败:', error);
        // 即使后端接口失败，也清除本地信息
        removeToken();
        removeNickName();
        removeUserName();
        removeUid();
        state.nickName = "";
        state.userName = "";
        state.token = "";
        ElMessage.warning("退出成功");
        router.push({ path: "/login" });
      }
    };
    return {
      ...toRefs(state),
      logo,
      searchByK,
      logout,
    };
  },
};
</script>