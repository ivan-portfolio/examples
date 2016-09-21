<?php

namespace Apollo\BusinessBundle\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\Controller;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Apollo\BusinessBundle\Entity\Business;
use Apollo\BusinessBundle\Form\Type\BusinessFormType;

class BusinessController extends Controller
{
    public function indexAction(Request $request)
    {
        return $this->render('ApolloBusinessBundle:Business:index.html.twig');
    }

    /**
     * Create a new Business
     *
     * @param Request Object Representation of the HTTP Request
     * @return Response
     */
    public function newAction(Requ  est $request)
    {
        // Create the form for a business entity
        $form = $this->container->get('apollo_business.business.new.form.factory')
            ->create();

        // Form Handler handles the form
        $formHandler = $this->container->get('apollo_business.business.new.form.handler');

        if ( $business = $formHandler->process($form) ) {
            $url = $this->generateUrl('apollo_business_new');

            $this->setFlash('success', 'Business Created Successfully');
            return $this->redirect($url);
        }

        return $this->render('ApolloBusinessBundle:Business:new.html.twig',
            array('form' => $form->createView())
        );
    }


    /**
     * Edit a Business
     *
     * @param int $id Id of the business
     * @param Request $request Object Representation of the HTTP Request
     * @return Response
     */
    public function editAction($id, Request $request)
    {
        // Get the business
        $business = $this->container->get('apollo_business.business.manager')
            ->find($id);

        // Create the form for a business entity
        $form = $this->container->get('apollo_business.business.edit.form.factory')
            ->create($business);

        // Form Handler handles the form
        $formHandler = $this->container->get('apollo_business.business.edit.form.handler');

        if ( $formHandler->process($form) ) {
            $url = $this->generateUrl('apollo_business_show', array('id'=>$id) );

            $this->setFlash('success', 'Business Edited Successfully');
            return $this->redirect($url);
        }

        return $this->render('ApolloBusinessBundle:Business:edit.html.twig',
            array(
                'business' => $business,
                'form' => $form->createView()
            )
        );
    }

    /**
     * Edit a Business
     *
     * @param int $id Id of the business
     * @param Request $request Object Representation of the HTTP Request
     * @return Response
     */
    public function showAction($id, Request $request)
    {
        // Get the business
        $business = $this->container->get('apollo_business.business.manager')
            ->find($id);

        return $this->render('ApolloBusinessBundle:Business:show.html.twig',
            array(
                'business' => $business
            )
        );
    }



    /**
     * @deprecated This is the old function
     */
    public function oldNewAction(Request $request)
    {
        // Create a new business object
        $business = new Business();
        // Create a form
        $form = $this->createForm(new BusinessFormType(), $business);

        // We bind the form
        $form->handleRequest($request);

        if ( 'POST' === $request->getMethod() )
        {
            // We validate the form
            if ($form->isValid()){

                // Entity Manager specific to our Business Entity
                $entityManager = $this->getDoctrine()->getManager();
                $entityManager->persist($business);
                $entityManager->flush();

            }
        }

         return $this->render('ApolloBusinessBundle:Business:new.html.twig',
            array('form' => $form->createView())
        );
    }

    /**
     *
     * Add a message to the flash bag in the session
     */
    protected function setFlash($action, $value)
    {
        $this->container->get('session')->getFlashBag()->add($action, $value);
    }

}
