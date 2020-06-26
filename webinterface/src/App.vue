<template>
  <div id="app">
    <Overview v-if="overview" :overview="overview" :time="time" />
  </div>
</template>

<script lang="ts">
import Vue from "vue";
import axios from "axios";
import Overview from "./components/Overview.vue";
import { IOverview } from "./models";
import './assets/reset.css';

const exampleData: any = {
  nextDelivery: 441937,
  teams: [
    {
      display: { text: "Rot" , style: {} },
      name: "red",
      rank: 0,
      flags: [],
      color: 11141120
    },
    {
      display: { text: "Grün", style: {} },
      name: "green",
      rank: 2,
      flags: [
        { rank: 1, protection: 0 },
        { rank: 1, protection: 0 }
      ],
      color: 5635925
    },
    {
      display: { text: "Blau", siblings: [], style: {} },
      name: "blue",
      rank: 0,
      flags: [],
      color: 5592575
    }
  ]
};

export default Vue.extend({
  name: "App",
  components: {
    Overview
  },
  data: () => ({
    overview: null,
    time: 0,
  }),
  mounted: function() {
    //this.fetchOverview();
    this.overview = exampleData;
    //window.setInterval(() => this.fetchTime(), 1000);
  },
  methods: {
    fetchOverview: function() {
      axios
        .get("/overview")
        .then(r => (this.overview = r.data))
        .catch(e => console.error(e));
    },
    fetchTime: function() {
      axios
        .get("/gametime")
        .then(r => (this.time = r.data.time))
        .catch(e => console.error(e));
    }
  }
});
</script>

<style lang="scss">
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  user-select: none;

  height: 100vh;

  display: grid;
  justify-content: center;
  align-content: center;

  background-image: url('assets/dirt.png');
  background-repeat: repeat;
  background-size: 128px;
  image-rendering: pixelated;
}
</style>
