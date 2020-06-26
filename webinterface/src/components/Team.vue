<template>
  <tr>
    <td>{{ team.display.text }}</td>
    <td class="flags">
      <Flag v-for="(flag, i) in team.flags" :key="i" :flag="flag" :color="color" />
    </td>
    <td>
      <span class="rank">{{ team.rank }}</span>
    </td>
  </tr>
</template>

<script lang="ts">
import Vue, { PropType } from "vue";
import { ITeam } from "../models";
import Flag from "./Flag.vue";

export default Vue.extend({
  name: "Team",
  components: {
    Flag
  },
  props: {
    team: Object as PropType<ITeam>
  },
  computed: {
    color: function(this: { team: ITeam }) {
      if (!this.team.color) return null;
      const c = this.team.color;
      const r = (c >> 16) & 0xff;
      const g = (c >> 8) & 0xff;
      const b = (c >> 0) & 0xff;
      return `rgb(${r},${g},${b})`;
    }
  }
});
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss">
tr {
  td {
    padding: 30px;

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

  &:not(:last-child) {
    margin-bottom: 10px;
  }

  .rank {
    background-color: rgb(207, 159, 25);
    color: white;
    padding: 10px;
    border-radius: 1000px;
  }
}
</style>
