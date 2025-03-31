// 课程科目选项
export const SUBJECT_OPTIONS = [
  { value: '语文', label: '语文' },
  { value: '数学', label: '数学' },
  { value: '英语', label: '英语' },
  { value: '物理', label: '物理' },
  { value: '化学', label: '化学' },
  { value: '生物', label: '生物' },
  { value: '历史', label: '历史' },
  { value: '地理', label: '地理' },
  { value: '政治', label: '政治' },
  { value: '音乐', label: '音乐' },
  { value: '美术', label: '美术' },
  { value: '体育', label: '体育' },
  { value: '计算机', label: '计算机' }
];

// 如果需要只获取科目名称列表
export const SUBJECTS = SUBJECT_OPTIONS.map(option => option.value); 