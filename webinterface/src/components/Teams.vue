<template>
  <table class="teams">
    <tbody>
      <Team v-for="team of teams" :key="team.name" :team="team" />
    </tbody>
  </table>
</template>

<script lang="ts">
import Vue, { PropType } from "vue";
import { ITeam } from "../models";
import Team from "./Team.vue";

export default Vue.extend({
  name: "Teams",
  components: {
    Team
  },
  props: {
    teams: Array as PropType<ITeam[]>
  }
});
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped lang="scss">
table {
  border-collapse: separate;
  border-spacing: 0 10px;
}
</style>
