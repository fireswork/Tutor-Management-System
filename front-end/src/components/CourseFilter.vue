<template>
  <a-select
    v-model:value="category"
    style="width: 120px"
    placeholder="选择科目"
    :options="SUBJECT_OPTIONS"
    allowClear
    @change="handleFilterChange"
  />
</template>

<script setup>
import { SUBJECT_OPTIONS } from '../utils/constants'
</script> 