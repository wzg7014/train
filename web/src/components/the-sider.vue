<template>
  <a-layout-sider width="200" style="background: #fff">
    <a-menu
        mode="inline"
        v-model:selectedKeys="selectedKeys2"
        v-model:openKeys="openKeys"
        :style="{ height: '100%', borderRight: 0 }"
    >
      <a-menu-item key="/welcome">
        <router-link to="/welcome">
          <coffee-outlined /> &nbsp; 欢迎
        </router-link>
      </a-menu-item>
      <a-menu-item key="/passenger">
        <router-link to="/passenger">
          <coffee-outlined /> &nbsp; 乘车人管理
        </router-link>
      </a-menu-item>
    </a-menu>
  </a-layout-sider>
</template>

<script>
import {defineComponent, ref} from 'vue';

export default defineComponent({
  name: "the-sider-view",
  setup() {

    return{
      selectedKeys2: ref(['1']),
      openKeys: ref(['sub1']),
    };
  },
});

</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>


</style>
