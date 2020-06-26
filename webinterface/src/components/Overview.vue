<template>
  <div>
    <Delivery :next="overview.nextDelivery" />
    <Teams :teams="overview.teams" />
  </div>
</template>

<script lang="ts">
import Vue, { PropType } from "vue";
import { IOverview } from "../models";
import Teams from "./Teams.vue";
import Delivery from "./Delivery.vue";

export default Vue.extend({
  name: "Overview",
  components: {
    Teams,
    Delivery
  },
  props: {
    overview: Object as PropType<IOverview>,
    time: Number,
  }
});
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss">
div {
    background-color: rgb(170, 170, 170);
    display: grid;
    justify-content: center;
    padding: 20px;

    box-shadow: 0 0 20px 0 #000;

    border-top: 2px solid #ddd;
    border-bottom: 2px solid #444;

    &:first-of-type {
      border-left: 2px solid #ddd;
      border-top-left-radius: 5px;
      border-bottom-left-radius: 5px;
    }

    &:last-of-type {
      border-right: 2px solid #444;
      border-top-right-radius: 5px;
      border-bottom-right-radius: 5px;
    }
}
</style>
