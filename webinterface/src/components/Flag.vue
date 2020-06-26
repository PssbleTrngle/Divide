<template>
  <span :style="{ background: color }">{{ flag.rank }}</span>
</template>

<script lang="ts">
import Vue, { PropType } from "vue";
import { IFlag } from '../models';

export default Vue.extend({
  name: "Flag",
  props: {
    flag: Object as PropType<IFlag>,
    color: String,
  }
});
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss">
span {
  padding: 5px;
  background: rgb(87, 87, 87);
  position: relative;
  color: white;

  &:not(:last-of-type) {
    margin-right: 10px;
  }

  &::after {
    content: '';
    position: absolute;
    width: 30%;
    height: 30%;
    background-color: rgb(136, 113, 63);
    top: 100%;
    left: 35%;
  }
}
</style>
