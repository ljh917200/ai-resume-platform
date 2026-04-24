<template>
  <router-view />
  <loading-overlay />
  <error-notification />
</template>

<script setup>
import LoadingOverlay from './components/LoadingOverlay.vue'
import ErrorNotification from './components/ErrorNotification.vue'
</script>

<style>
@import './styles/global.css';

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}
</style>