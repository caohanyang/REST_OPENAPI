import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {EditorComponent} from '../editor/editor.component'
import {StepComponent } from '../step/step.component';

const routes: Routes = [
  {
    path: '',
    component: StepComponent,
  },
  {
    path: 'openapi',
    component: EditorComponent,
  },
];

@NgModule({
  imports: [
    RouterModule.forChild(routes),
    RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class RouterRoutingModule { }
