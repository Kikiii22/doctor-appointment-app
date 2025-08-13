import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListDoctors } from './list-doctors';

describe('ListDoctors', () => {
  let component: ListDoctors;
  let fixture: ComponentFixture<ListDoctors>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListDoctors]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListDoctors);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
