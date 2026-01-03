 <template>
  <div class="footer">
    <div class="box_center cf">
      <div class="copyright">
        <ul>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
import { useRouter } from "vue-router";
import { getToken } from "@/utils/auth";
export default {
  name: "Footer",
  setup() {
    const router = useRouter();
    const goFeedBack = () => {
      if(!getToken()){
        router.push({name: 'login'});
      }else{
      router.push({name: 'feadback'});
      }
    };
    return {
      goFeedBack
    }
  },
  
};
</script>