import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'ag-grid-community/styles/ag-grid.css'
import 'ag-grid-community/styles/ag-theme-quartz.css'
import { ModuleRegistry, AllCommunityModule } from 'ag-grid-community'
ModuleRegistry.registerModules([AllCommunityModule])
import './styles/ag-grid-theme.css'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'
import permission from './directives/permission'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.use(ElementPlus, { size: 'default' })
app.directive('permission', permission)

const auth = useAuthStore()
auth.initFromStorage()

app.mount('#app')
