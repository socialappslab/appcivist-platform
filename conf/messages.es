# Override default Play's validation messages

# --- Constraints
constraint.required=Required
constraint.min=Minimum value: {0}
constraint.max=Maximum value: {0}
constraint.minLength=Minimum length: {0}
constraint.maxLength=Maximum length: {0}
constraint.email=Email

# --- Formats
format.date=Date (''{0}'')
format.numeric=Numeric
format.real=Real

# --- Errors
error.invalid=Invalid value
error.required=This field is required
error.number=Numeric value expected
error.real=Real number value expected
error.min=Must be greater or equal to {0}
error.max=Must be less or equal to {0}
error.minLength=Minimum length is {0}
error.maxLength=Maximum length is {0}
error.email=Valid email required
error.pattern=Must satisfy {0}

### --- play-authenticate START

# play-authenticate: Initial translations

playauthenticate.accounts.link.success=Account linked successfully
playauthenticate.accounts.merge.success=Accounts merged successfully

playauthenticate.verify_email.error.already_validated=Your e-mail has already been validated.
playauthenticate.verify_email.error.set_email_first=You need to set an e-mail address first.
playauthenticate.verify_email.message.instructions_sent=Instructions on how to verify your e-mail address have been sent to {0}.
playauthenticate.verify_email.success=E-mail address ({0}) successfully verified.

playauthenticate.reset_password.message.instructions_sent=Instructions on how to reset your password have been sent to {0}.
playauthenticate.reset_password.message.email_not_verified=Your account has not been verified, yet. An e-mail including instructions on how to verify it has been sent out. Retry resetting your password afterwards.
playauthenticate.reset_password.message.no_password_account=Your user has not yet been set up for password usage.
playauthenticate.reset_password.message.success.auto_login=Your password has been reset.
playauthenticate.reset_password.message.success.manual_login=Your password has been reset. Please now log in using your new password.

playauthenticate.change_password.error.passwords_not_same=Passwords do not match.
playauthenticate.change_password.success=Password has been changed successfully.

playauthenticate.password.signup.error.passwords_not_same=Las passwords no coinciden
playauthenticate.password.login.unknown_user_or_pw=Usuario o password desconocidos
playauthenticate.password.signup.error.missing_lang=Falta elegir un idioma
playauthenticate.password.signup.error.password_null=Password not provided.
playauthenticate.password.signup.error.password_repeat_null=Password not repeated.


playauthenticate.password.verify_signup.subject=AppCivist: Completa tu subscripción
playauthenticate.password.verify_email.subject=AppCivist: Confirma tu dirección de correo electrónico
playauthenticate.password.reset_email.subject=AppCivist: Como recuperar tu password

# play-authenticate: Additional translations

playauthenticate.login.email.placeholder=Your e-mail address
playauthenticate.login.password.placeholder=Choose a password
playauthenticate.login.password.repeat=Repeat chosen password
playauthenticate.login.title=Login
playauthenticate.login.password.placeholder=Password
playauthenticate.login.now=Login now
playauthenticate.login.forgot.password=Forgot your password?
playauthenticate.login.oauth=or log in using one of the following providers:

playauthenticate.signup.title=Signup
playauthenticate.signup.name=Your name
playauthenticate.signup.now=Sign up now
playauthenticate.signup.oauth=or sign up using one of the following providers:
playauthenticate.signup.form-has-errors=El formulario de registro tiene errores. Puede ser que el email sea inválido, las passwords no coincidan o la información no sea completa.

playauthenticate.verify.account.title=E-mail verification required
playauthenticate.verify.account.before=Before setting a password, you need to
playauthenticate.verify.account.first=first verify your e-mail address

playauthenticate.change.password.title=Change your password here
playauthenticate.change.password.cta=Change my password

playauthenticate.merge.accounts.title=Merge accounts
playauthenticate.merge.accounts.question=Do you want to merge your current account ({0}) with this account: {1}?
playauthenticate.merge.accounts.true=Yes, merge these two accounts
playauthenticate.merge.accounts.false=No, exit my current session and log in as a new user
playauthenticate.merge.accounts.ok=OK

playauthenticate.link.account.title=Link account
playauthenticate.link.account.question=Link ({0}) with your user?
playauthenticate.link.account.true=Yes, link this account
playauthenticate.link.account.false=No, log out and create a new user with this account
playauthenticate.link.account.ok=OK

# play-authenticate: Signup folder translations

playauthenticate.verify.email.title=Verify your e-mail
playauthenticate.verify.email.requirement=Before you can use PlayAuthenticate, you first need to verify your e-mail address.
playauthenticate.verify.email.cta=An e-mail has been sent to the registered address. Please follow the embedded link to activate your account.

playauthenticate.password.reset.title=Reset password
playauthenticate.password.reset.cta=Reset my password

playauthenticate.password.forgot.title=Forgot password
playauthenticate.password.forgot.cta=Send reset instructions

playauthenticate.oauth.access.denied.title=OAuth access denied
playauthenticate.oauth.access.denied.explanation=If you want to use PlayAuthenticate with OAuth, you must accept the connection.
playauthenticate.oauth.access.denied.alternative=If you rather not like to do this, you can also
playauthenticate.oauth.access.denied.alternative.cta=sign up with a username and password instead

playauthenticate.token.error.title=Token error
playauthenticate.token.error.message=The given token has either expired or does not exist.

playauthenticate.user.exists.title=User exists
playauthenticate.user.does.not.exists.title=User does not exists
playauthenticate.user.exists.message=This user already exists.

# play-authenticate: Navigation
playauthenticate.navigation.profile=Profile
playauthenticate.navigation.link_more=Link more providers
playauthenticate.navigation.logout=Sign out
playauthenticate.navigation.login=Log in
playauthenticate.navigation.home=Home
playauthenticate.navigation.restricted=Restricted page
playauthenticate.navigation.signup=Sign up

# play-authenticate: Handler
playauthenticate.handler.loginfirst=You need to log in first, to view ''{0}''

# play-authenticate: Profile
playauthenticate.profile.title=User profile
playauthenticate.profile.mail=Your name is {0} and your email address is {1}!
playauthenticate.profile.unverified=unverified - click to verify
playauthenticate.profile.verified=verified
playauthenticate.profile.providers_many=There are {0} providers linked with your account:
playauthenticate.profile.providers_one = There is one provider linked with your account:
playauthenticate.profile.logged=You are currently logged in with:
playauthenticate.profile.session=Your user ID is {0} and your session will expire on {1}
playauthenticate.profile.session_endless=Your user ID is {0} and your session will not expire, as it is endless
playauthenticate.profile.password_change=Change/set a password for your account

# play-authenticate - sample: Index page
playauthenticate.index.title=Welcome to Play Authenticate
playauthenticate.index.intro=Play Authenticate sample app
playauthenticate.index.intro_2=This is a template for a simple application with authentication.
playauthenticate.index.intro_3=Check the main navigation above for simple page examples including supported authentication features.
playauthenticate.index.heading=Heading
playauthenticate.index.details=View details
# play-authenticate - sample: Restricted page
playauthenticate.restricted.secrets=Secrets, everywhere!

### --- play-authenticate END

### --- AppCivist Messages
groups.creation.success = El grupo de trabajo {0} ha sido creado con exito por {1}
groups.creation.error = El grupo de trabajo no fue creado. Hubo un problema con la peticion: {0}
assemblies.creation.success = La asamblea {0} ha sido creada con exito por {1}
assemblies.creation.error = La asamblea no fue creada. Hubo un problema con la peticion: {0}
roles.creation.success = El rol {0} ha sido creado con exito por {1}
roles.creation.error = El rol no fue creado. Hubo un problema con la peticion: {0}
config.creation.success = La config {0} ha sido creada con exito por {1}
config.creation.error = La config no fue creada. Hubo un problema con la peticion: {0}
campaign.creation.success = La campaña {0} ha sido creada con exito por {1}
campaign.creation.error = La campaña no fue creada. Hubo un problema con la peticion: {0}
campaign.phase.creation.success = La fase de campaña {0} ha sido creada con exito por {1}
campaign.phase.creation.error = La fase de campaña no fue creada. Hubo un problema con la peticion: {0}
membership.invitation.creation.success = La invitacion a la membresia {0} ha sido creada con exito por {1}
membership.invitation.creation.error = La invitacion a la membresia no fue creada. Hubo un problema con la peticion: {0}
membership.invitation.email.message = Invitacion para membresia
membership.invitation.creation.unauthorized = La membresía no pudo ser creada porque el usuario no está autorizado
membership.invitation.email.subject=Invitación para unirse a AppCivist {0}
membership.request.email.subject=Petición para unirse a AppCivist {0}
membership.confirmation.email.subject=Bienvenido a AppCivist {0}

notification.title.assembly.update=Nuevo en Asamblea
notification.title.group.update=Nuevo en Grupo de Trabajo
notification.title.contribution.update=Nuevo en Contribución
notification.title.campaign.update=Nuevo en Campaña
notification.title.campaign.update.milestone=Próximos Plazos
notification.title.message.new=Nuevo Mensaje
notification.title.message.reply=Nueva Respuesta
notification.title.message.new.group=Nuevo Mensaje para Grupo de Trabajo
notification.title.message.reply.group=Nueva Respuesta para Grupo de Trabajo
notification.title.message.new.assembly=Nuevo Mensaje para Asamblea
notification.title.message.reply.assembly=Nueva Respuesta para Asamblea
contribution.previous.proposal.document=Documento de propuesta anterior
contribution.etherpad.words=palabras

### Mail notifications
mail.notification.unsubscribe=Desuscribirse
mail.notification.new_activity=Nueva Actividad
mail.notification.date_text=Notificación Semanal
mail.notification.campaign_description_text=Descripción de la Campaña
mail.notification.stage_name_text=está recogiendo contribuciones para los siguientes temas:
mail.notification.campaign_name_text=tiene los siguientes grupos de trabajo:
mail.notification.more_information_text=Más información sobre la campaña puede encontrarse en:
mail.notification.campaign_description_text.no_activity=Esta campaña está actualmente en la etapa de recolección de ideas. ¡Evía una ahora!
mail.notification.new_ideas_number_text= nuevas ideas han sido enviadas la semana pasada:
mail.notification.updates_text=Aquí están las actualizaciones de las ideas que sigues:
mail.notification.campaign_description_text.proposal=Esta campaña está actualmente en la etapa de creación de propuestas
mail.notification.new_proposal_text=Tu grupo de trabajo {0} ha enviado {1} nuevas propuestas:
mail.notification.proposal_developing_text=Tu grupo de trabajo {0} está desarrollando actualmente las siguientes propuestas:
mail.notification.updates_text.proposal=Resumen de los otros grupos de trabajo

contribution.unauthorized.creation = No se puede agregar un nuevo {0} en este espacio porque esta actualmente en estado DRAFT privado

mail.notification.add.nonmember=Te han invitado a colaborar en la {0} conectada al link de abajo para el proceso participativo {1}. Accede a la iniciativa con este enlace y luego inicia sesión. Luego de iniciar sesión, podrás encontrar la {3} a través "Mis {2}". Si no quieres formar parte de esta proposta, elimine su nombre de la lista de autores o contacte al autor principal. Link: <a href="{4}">{4}</a>
mail.notification.add.nonmember.subject=[AppCivist] Invitación para colaborar en {0}

appcivist.contribution = contribución
appcivist.contribution.proposal = propuesta
appcivist.contribution.idea = idea
appcivist.contribution.proposals = propuestas
appcivist.contribution.ideas = ideas

appcivist.contribution.change.status = Debe completar todos los campos requeridos: {0} antes de pasar al estado {1}

appcivist.contribution.status.forked_private_draft = Private Draft Amendment
appcivist.contribution.status.forked_public_draft = Public Draft Amendment
appcivist.contribution.status.forked_published = Published Dissensus Amendment
appcivist.contribution.status.merged = Merged Published Amendment
appcivist.contribution.status.public_draft = Public Draft
appcivist.contribution.status.published = Published