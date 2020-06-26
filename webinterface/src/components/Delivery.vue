<template>
  <p class="delivery">{{ countdown }}</p>
</template>

<script lang="ts">
import Vue, { PropType } from "vue";

export default Vue.extend({
  name: "Delivery",
  props: {
    next: Number,
    time: Number
  },
  computed: {
    countdown: function(this: { next: number; time: number }) {
      const diff = this.next - this.time;
      return isNaN(diff) ? "Loading..." : diff;
    }
  }
});
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss">
p {
  padding: 10px;
  text-align: center;
  border: 1px dashed rgb(46, 119, 255);
  background-color: rgb(46, 119, 255, 0.25);
  border-radius: 5px;
}
</style>
