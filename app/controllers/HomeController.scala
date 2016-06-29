
package controllers

import javax.inject._

import models.Employee
import play.api.Play.current
import play.api.data.Forms._
import play.api.data._
import play.api.db._
import play.api.i18n.Messages.Implicits._
import play.api.mvc._

import scala.collection.mutable

/**
 *
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(db:Database) extends Controller {

  def index = Action {
    Ok(views.html.index(loginForm))
  }


  def fetch() = Action{
    val list = mutable.MutableList[Employee]()
    val conn = db.getConnection()
    try {
        val stmt = conn.createStatement()
        val result = stmt.executeQuery(
          """
          select * from employee
        """.stripMargin)
        while (result.next()) {
          list.+=(Employee(result.getString(1), result.getInt(2), result.getString(3), result.getString(4),
            result.getString(5), result.getString(6), result.getString(7), result.getInt(8)))

        }
    }finally {
      conn.close()
    }
      Ok(views.html.fetch(list))
    }



  
  def about = Action {
    Ok(views.html.about())
  }


  def doLogin = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.index(formWithErrors)),
      employee => {
        HomeController.this.add(employee)
        Ok(views.html.submitted(employee))
      })

  }

  def add(employee: Employee){
    val conn = db.getConnection()
    val query = "INSERT INTO employee(name,age,sex,address,email,education,maritalStatus,salary) " +
      "VALUES(?,?,?,?,?,?,?,?)"
    try {
      val stmt = conn.prepareStatement(query)
      stmt.setString(1, employee.name)
      stmt.setInt(2, employee.age)
      stmt.setString(3, employee.sex)
      stmt.setString(4, employee.address)
      stmt.setString(5, employee.email)
      stmt.setString(6, employee.education)
      stmt.setString(7, employee.maritalStatus)
      stmt.setInt(8,employee.salary)
      stmt.execute()
    }finally {
      conn.close()
    }
  }

  def loginForm = Form(
    mapping(
      "Employee Name" -> nonEmptyText,
      "Age" -> number(min = 16,max = 60),
      "Sex" -> text,
      "Address" -> nonEmptyText,
      "Email" -> email,
      "Education" -> nonEmptyText,
      "Marital Status" ->text,
      "Salary (in K)" -> number(min = 10 , max = 100)
    )(Employee.apply)(Employee.unapply)
  )

}
 
